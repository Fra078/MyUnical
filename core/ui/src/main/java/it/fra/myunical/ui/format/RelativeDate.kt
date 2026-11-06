package it.fra.myunical.ui.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SameYearFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)
private val OtherYearFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

/** "Oggi", "Ieri", "5 nov" or "5 nov 2025" when not in the current year. */
fun LocalDate.toRelativeLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Oggi"
    today.minusDays(1) -> "Ieri"
    else -> format(if (year == today.year) SameYearFormatter else OtherYearFormatter)
}
