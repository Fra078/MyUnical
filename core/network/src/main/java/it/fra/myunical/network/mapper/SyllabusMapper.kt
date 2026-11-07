package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.OfferedActivity
import it.fra.myunical.domain.model.syllabus.SyllabusSection
import it.fra.myunical.domain.model.syllabus.SyllabusSectionKind
import it.fra.myunical.domain.model.syllabus.SyllabusTeacher
import it.fra.myunical.domain.model.syllabus.TeachingHours
import it.fra.myunical.network.models.syllabus.StudyActivityContentDto
import it.fra.myunical.network.models.syllabus.StudyActivityDetailDto
import it.fra.myunical.network.models.syllabus.StudyActivityListItemDto

private const val TEAMS_LINK = "LINK_TEAMS"
private const val TEAMS_CODE = "CODICE_TEAMS"

fun StudyActivityDetailDto.toDomainModel(): CourseSyllabus {
    val texts = contents.mapNotNull { content -> content.cleanText()?.let { content.code to it } }.toMap()
    return CourseSyllabus(
        activityId = id,
        code = code,
        name = name.trim(),
        partition = partition?.trim()?.ifBlank { null },
        degreeCourse = degreeCourse?.trim()?.ifBlank { null },
        credits = credits,
        ssdCode = ssdCode?.ifBlank { null },
        ssd = ssd?.ifBlank { null },
        courseYear = year,
        semester = semester?.ifBlank { null },
        languages = languages.filter(String::isNotBlank),
        teachingType = teachingUnitType?.ifBlank { null },
        isCompulsory = when (compulsory?.trim()?.lowercase()) {
            "si", "sì" -> true
            "no" -> false
            else -> null
        },
        teachers = teachers(),
        sections = SyllabusSectionKind.entries.mapNotNull { kind -> texts[kind.code]?.let { SyllabusSection(kind, it) } },
        teamsLink = texts[TEAMS_LINK]?.takeIf { it.startsWith("http") },
        teamsCode = texts[TEAMS_CODE],
        borrowedFromId = borrowedFrom?.id?.takeIf { it != id }
    )
}

/** One entry per teacher (the API repeats them per activity type), in the order they appear. */
private fun StudyActivityDetailDto.teachers(): List<SyllabusTeacher> =
    hours
        .filter { it.teacherName != null || it.teacherId != null }
        .groupBy { it.teacherId ?: it.teacherName }
        .map { (_, rows) ->
            val first = rows.first()
            SyllabusTeacher(
                id = first.teacherId?.ifBlank { null },
                name = first.teacherName?.toTitleCase()?.ifBlank { null } ?: "Docente da definire",
                hours = rows.mapNotNull { row ->
                    val type = row.type?.ifBlank { null } ?: return@mapNotNull null
                    row.hours?.takeIf { it > 0 }?.let { TeachingHours(type, it) }
                }
            )
        }

private val LineBreaks = Regex("\r\n|\r|\u2028|\u2029")
private val BlankLines = Regex("\n[ \t]*(\n[ \t]*)+")
private val HtmlTags = Regex("<[^>]+>")

/**
 * Text pasted from documents (newer pages use HTML): line separators become new lines and runs of
 * blank lines one empty line. Contents left empty or filled with a placeholder ("-", ".", "<p></p>")
 * are skipped.
 */
private fun StudyActivityContentDto.cleanText(): String? =
    text?.replace(LineBreaks, "\n")
        ?.replace(BlankLines, "\n\n")
        ?.trim()
        ?.takeIf { value -> value.replace(HtmlTags, "").any(Char::isLetterOrDigit) }

fun StudyActivityListItemDto.toDomainModel() = OfferedActivity(
    activityId = id,
    code = code,
    name = name.trim(),
    cdsCode = cdsCode,
    partition = partition?.trim()?.ifBlank { null },
    semester = semester?.ifBlank { null }
)
