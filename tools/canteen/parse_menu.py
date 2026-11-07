#!/usr/bin/env python3
"""
Converts the Unical canteen menu PDFs into the JSON read by the app.

    python3 tools/canteen/parse_menu.py menus/pdf/Menu_Mense_Unical_dal_5_ottobre_al_15_novembre_2026.pdf

Each PDF holds two landscape pages per week (lunch, then dinner). On every page each day has 5 rows
in each section (first courses, mains, sides), anchored to a line code (C, V, M/PF, NML). The diet
icons are not text: they are cells filled with one flat colour per type, read from a render.

The days covered by the PDF replace the ones already in the JSON; days older than 30 days before
it are dropped. The renders of the pages are written to menus/pages/ to check the result.

Needs poppler (pdftotext, pdftoppm, pdfinfo) and Pillow.
"""

import argparse
import datetime as dt
import html
import json
import re
import subprocess
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "core/datastore/src/main/assets/canteen/menu.json"
PAGES_DIR = ROOT / "menus/pages"

DPI = 150
SCALE = DPI / 72
KEEP_DAYS_BEFORE = 30

MONTHS = {m: i + 1 for i, m in enumerate([
    "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
    "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre"
])}
DAYS = ["LUNEDÌ", "MARTEDÌ", "MERCOLEDÌ", "GIOVEDÌ", "VENERDÌ", "SABATO", "DOMENICA"]
MEALS = {"PRANZO": "LUNCH", "CENA": "DINNER"}
COURSES = ["FIRST", "SECOND", "SIDE"]
LINES = {"C": "CLASSIC", "V": "VEGETARIAN", "M": "SOUP", "PF": "COLD", "NML": "MEDITERRANEAN"}

# Flat fill of each diet column, from the legend of the PDF
TAG_COLORS = {
    "LIGHT": (0, 176, 240),
    "GLUTEN_FREE": (226, 107, 10),
    "PORK": (192, 80, 77),
    "VEGETARIAN": (79, 98, 40),
    "LACTOSE_FREE": (49, 134, 155),
    "VEGAN": (155, 187, 89),
}
COLOR_TOLERANCE = 18

# The PDF sometimes marks meat or fish as vegetarian: those tags are dropped (and reported)
NOT_VEGETARIAN = re.compile(
    r"\b(pollo|tacchino|maiale|suin\w*|prosciutto|speck|pancetta|guanciale|salsiccia|salame|wurstel|"
    r"bovino|manzo|vitell\w*|bufalo|agnello|coniglio|carne|ragù(?! (?:vegetale|vegetariano|di lenticchie|di soia))|arista|roast|braciol\w*|polpett\w* di carne|"
    r"pesce|tonno|salmone|merluzzo|platessa|alici|alaccia|sgombro|seppi\w*|calamar\w*|cozze|vongole|gamber\w*|"
    r"pescatora|mare|nasello|orata|spigola|baccalà|acciughe|sardin\w*|totan\w*|cacciucco)\b",
    re.IGNORECASE,
)
# A cell is a few points wide: shorter runs are anti-aliasing or coloured text
MIN_RUN_PT = 3

WORD_RE = re.compile(
    r'<word xMin="([\d.]+)" yMin="([\d.]+)" xMax="([\d.]+)" yMax="([\d.]+)">(.*?)</word>'
)


class Word:
    def __init__(self, x0, y0, x1, y1, text):
        self.x0, self.y0, self.x1, self.y1, self.text = x0, y0, x1, y1, text

    @property
    def yc(self):
        return (self.y0 + self.y1) / 2


def run(*args):
    return subprocess.run(args, check=True, capture_output=True, text=True).stdout


def page_count(pdf):
    return int(re.search(r"Pages:\s+(\d+)", run("pdfinfo", str(pdf))).group(1))


def words_of(pdf, page):
    out = run("pdftotext", "-bbox", "-f", str(page), "-l", str(page), str(pdf), "-")
    return [Word(float(a), float(b), float(c), float(d), html.unescape(t)) for a, b, c, d, t in WORD_RE.findall(out)]


def render(pdf, page, stem):
    PAGES_DIR.mkdir(parents=True, exist_ok=True)
    prefix = PAGES_DIR / f"{stem}-p{page:02d}"
    run("pdftoppm", "-r", str(DPI), "-png", "-singlefile", "-f", str(page), "-l", str(page), str(pdf), str(prefix))
    return Image.open(f"{prefix}.png").convert("RGB")


def default_year(pdf):
    match = re.search(r"(20\d\d)", pdf.name)
    if match:
        return int(match.group(1))
    created = re.search(r"CreationDate:\s+.*?(20\d\d)", run("pdfinfo", str(pdf)))
    return int(created.group(1)) if created else dt.date.today().year


def week_range(words, year):
    """'MENÙ ORDINARIO - Dal 5 Ottobre al 11 Ottobre' → (start, end)."""
    title = " ".join(w.text for w in sorted(words, key=lambda w: (round(w.y0), w.x0)) if w.y0 < 85)
    match = re.search(r"Dal\s+(\d+)\s+(\w+)\s+al\s+(\d+)\s+(\w+)", title, re.IGNORECASE)
    if not match:
        raise ValueError(f"week not found in title: {title!r}")
    d1, m1, d2, m2 = match.groups()
    m1, m2 = MONTHS[m1.lower()], MONTHS[m2.lower()]
    start = dt.date(year, m1, int(d1))
    end = dt.date(year + (1 if m2 < m1 else 0), m2, int(d2))
    return start, end


def section_columns(words):
    """x of the line codes of the three sections, left to right."""
    # The legend at the bottom left repeats the codes: only the ones right of the day names count
    codes = sorted(w.x0 for w in words if w.text in LINES and w.x1 - w.x0 < 14 and w.y0 > 98 and w.x0 > 60)
    columns = []
    for x in codes:
        if not columns or x - columns[-1][-1] > 30:
            columns.append([x])
        else:
            columns[-1].append(x)
    if len(columns) != 3:
        raise ValueError(f"expected 3 sections, found {len(columns)}")
    return [min(c) for c in columns]


def parse_page(pdf, page, year, image, warnings):
    words = words_of(pdf, page)
    meal = next((MEALS[w.text] for w in words if w.text in MEALS), None)
    if meal is None:
        raise ValueError(f"page {page}: meal (PRANZO/CENA) not found")
    start, end = week_range(words, year)
    columns = section_columns(words)
    width = columns[1] - columns[0]
    bounds = [(x - 4, (columns[i + 1] if i < 2 else x + width) - 4) for i, x in enumerate(columns)]
    footer_y = min((w.y0 for w in words if w.text.startswith("*") and w.x0 < 60), default=10_000)

    day_labels = sorted((w for w in words if w.text in DAYS and w.x0 < columns[0]), key=lambda w: w.yc)
    if [w.text for w in day_labels] != DAYS:
        warnings.append(f"page {page}: days found {[w.text for w in day_labels]}")

    days = {}
    for index, (left, right) in enumerate(bounds):
        section = [w for w in words if left <= w.x0 < right and 98 < w.y0 < footer_y]
        codes = sorted((w for w in section if w.text in LINES and w.x0 < left + 14), key=lambda w: w.yc)
        text_words = [w for w in section if w not in codes]
        if len(codes) != 35:
            warnings.append(f"page {page} {COURSES[index]}: {len(codes)} rows instead of 35")

        # Each text word goes to the nearest row: wrapped names end up on the same dish
        names = {id(c): [] for c in codes}
        for w in text_words:
            row = min(codes, key=lambda c: abs(c.yc - w.yc))
            if abs(row.yc - w.yc) > 9:
                warnings.append(f"page {page}: '{w.text}' far from any row, skipped")
                continue
            names[id(row)].append(w)

        for position, code in enumerate(codes):
            date = start + dt.timedelta(days=position // 5)
            name = " ".join(w.text for w in sorted(names[id(code)], key=lambda w: (round(w.yc), w.x0)))
            name = re.sub(r"\s+", " ", name).strip()
            frozen = "*" in name
            name = re.sub(r"\s*\*\s*", " ", name).replace(" ,", ",").strip()
            tags = row_tags(image, code, left, right)
            meat = NOT_VEGETARIAN.search(name)
            if meat and {"VEGETARIAN", "VEGAN"} & set(tags):
                warnings.append(f"page {page} {date}: '{name}' marked vegetarian/vegan but has '{meat.group(0)}', tag dropped")
                tags = [t for t in tags if t not in ("VEGETARIAN", "VEGAN")]
            if not name:
                warnings.append(f"page {page} {date} {COURSES[index]}: empty dish")
            day = days.setdefault(date, {c: [] for c in COURSES})
            day[COURSES[index]].append({
                "name": name[:1].upper() + name[1:],
                "line": LINES[code.text],
                "tags": tags,
                "frozen": frozen,
            })
    if max(days) != end:
        warnings.append(f"page {page}: last day {max(days)} but the title says {end}")
    drop_copied_sides(page, days, warnings)
    return meal, days


def drop_copied_sides(page, days, warnings):
    """Some pages repeat the mains in the sides column: better no sides than wrong ones."""
    rows = [(s["name"], d["name"]) for day in days.values() for s, d in zip(day["SECOND"], day["SIDE"])]
    copied = sum(1 for second, side in rows if second.lower() == side.lower())
    if rows and copied / len(rows) >= 0.8:
        warnings.append(f"page {page}: sides are a copy of the mains in the PDF ({copied}/{len(rows)}), left out")
        for day in days.values():
            day["SIDE"] = []


def row_tags(image, code, left, right):
    """Diet tags of a row: flat colour runs between the dish name and the next section."""
    y = int(code.yc * SCALE)
    found = []
    for tag, color in TAG_COLORS.items():
        run_length = best = 0
        for x in range(int((left + 14) * SCALE), int(right * SCALE)):
            pixel = image.getpixel((x, y))
            if all(abs(a - b) <= COLOR_TOLERANCE for a, b in zip(pixel, color)):
                run_length += 1
                best = max(best, run_length)
            else:
                run_length = 0
        if best >= MIN_RUN_PT * SCALE:
            found.append(tag)
    return found


def parse_pdf(pdf, year):
    warnings = []
    menu = {}
    for page in range(1, page_count(pdf) + 1):
        image = render(pdf, page, pdf.stem)
        meal, days = parse_page(pdf, page, year, image, warnings)
        tags = sum(len(d["tags"]) for day in days.values() for c in day.values() for d in c)
        print(f"page {page:2d}: {meal:6s} {min(days)} → {max(days)}, "
              f"{sum(len(c) for day in days.values() for c in day.values())} dishes, {tags} tags")
        for date, courses in days.items():
            meals = menu.setdefault(date.isoformat(), {})
            if meal in meals:
                warnings.append(f"page {page}: {meal} of {date} already read")
            meals[meal] = courses
    return menu, warnings


def to_json_days(menu):
    order = list(MEALS.values())
    return [{
        "date": date,
        "meals": [{
            "type": meal,
            "courses": [{"type": course, "dishes": dishes} for course, dishes in meals[meal].items()]
        } for meal in order if meal in meals]
    } for date, meals in sorted(menu.items())]


def merge(pdf, menu):
    current = json.loads(OUTPUT.read_text()) if OUTPUT.exists() else {"version": 1, "sources": [], "days": []}
    first, last = min(menu), max(menu)
    keep_from = (dt.date.fromisoformat(first) - dt.timedelta(days=KEEP_DAYS_BEFORE)).isoformat()
    days = [d for d in current["days"] if keep_from <= d["date"] and not first <= d["date"] <= last]
    sources = [s for s in current["sources"] if s["file"] != pdf.name and s["to"] >= keep_from]
    sources.append({"file": pdf.name, "from": first, "to": last})
    return {
        "version": 1,
        "generatedAt": dt.date.today().isoformat(),
        "sources": sorted(sources, key=lambda s: s["from"]),
        "days": sorted(days + to_json_days(menu), key=lambda d: d["date"]),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("pdf", type=Path)
    parser.add_argument("--year", type=int, help="year of the first week (default: from the file name)")
    parser.add_argument("--dry-run", action="store_true", help="parse and report without writing the JSON")
    args = parser.parse_args()

    menu, warnings = parse_pdf(args.pdf, args.year or default_year(args.pdf))
    for w in warnings:
        print("WARNING:", w, file=sys.stderr)
    if args.dry_run:
        return
    result = merge(args.pdf, menu)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(result, ensure_ascii=False, indent=1) + "\n")
    print(f"{OUTPUT.relative_to(ROOT)}: {len(result['days'])} days, "
          f"{result['days'][0]['date']} → {result['days'][-1]['date']}")


if __name__ == "__main__":
    main()
