package it.fra.myunical.career.mapper

import it.fra.myunical.career.models.SyllabusFact
import it.fra.myunical.career.models.SyllabusSectionUiModel
import it.fra.myunical.career.models.SyllabusUiModel
import it.fra.myunical.career.models.TeacherUiModel
import it.fra.myunical.career.models.UnitOption
import it.fra.myunical.career.models.WorkloadUiModel
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.CourseUnit
import it.fra.myunical.domain.model.syllabus.SyllabusSectionKind
import java.util.Locale

/** "1° anno · A.A. 2024/25" */
fun Course.detailSubtitle(): String = listOfNotNull(
    year.takeIf { it > 0 }?.let { "$it° anno" },
    attendanceAcademicYear.takeIf { it > 0 }?.let { "A.A. $it/${(it + 1) % 100}" },
    courseType.takeIf { it.isNotBlank() }?.capitalized()
).joinToString(" · ")

private val ModulePrefix = Regex("^\\s*(modulo\\s*\\d+)\\s*[:.\\-–]?\\s*", RegexOption.IGNORE_CASE)

/**
 * Chips stay short: modules by their number ("Modulo 1", full name as [UnitOption.title]),
 * groups by their partition ("Gruppo A").
 */
fun List<CourseUnit>.toOptions(): List<UnitOption> {
    val isModular = map { it.name }.distinct().size > 1
    return mapIndexed { index, unit ->
        val prefix = ModulePrefix.find(unit.name)
        val moduleName = (prefix?.let { unit.name.removeRange(it.range) } ?: unit.name).trim().readableCase()
        val moduleLabel = prefix?.groupValues?.get(1)?.capitalized() ?: moduleName
        val label = listOfNotNull(moduleLabel.takeIf { isModular }, unit.partition)
            .joinToString(" · ")
            .ifBlank { "Parte ${index + 1}" }
        UnitOption(unit.activityId, label, title = moduleName.takeIf { isModular && it.isNotBlank() })
    }
}

/** "IoT SENSOR DEVICE PROGRAMMING" → "IoT Sensor Device Programming": words in capitals are title-cased. */
private fun String.readableCase(): String = split(' ').joinToString(" ") { word ->
    if (word.length > 1 && word.none(Char::isLowerCase)) word.capitalized() else word
}

fun CourseSyllabus.toUiModel() = SyllabusUiModel(
    activityId = activityId,
    facts = listOfNotNull(
        ssdCode?.let { code -> SyllabusFact("Settore", listOfNotNull(code, ssd?.capitalized()).joinToString(" · ")) },
        semester?.let { SyllabusFact("Periodo", it) },
        languages.takeIf { it.isNotEmpty() }?.let { SyllabusFact("Lingua", it.joinToString(", ")) },
        teachingType?.let { SyllabusFact("Attività", it) },
        isCompulsory?.let { SyllabusFact("Obbligatorio", if (it) "Sì" else "No") },
        degreeCourse?.let { SyllabusFact("Corso di studi", it.toTitleCase()) }
    ),
    workload = WorkloadUiModel(
        credits = credits?.formatCredits(),
        totalHours = totalHours,
        hours = hoursByType.map { it.type.activityTypeLabel() to it.hours },
        estimate = section(SyllabusSectionKind.WORKLOAD)
    ),
    teachers = teachers.map { teacher ->
        TeacherUiModel(
            id = teacher.id,
            name = teacher.name,
            hours = teacher.hours
                .joinToString(" · ") { "${it.type.activityTypeLabel()} ${it.hours} h" }
                .ifBlank { null }
        )
    },
    sections = sections
        .filter { it.kind != SyllabusSectionKind.WORKLOAD }
        .map { SyllabusSectionUiModel(it.kind.title(), it.text) },
    teamsLink = teamsLink,
    teamsCode = teamsCode
)

private fun Double.formatCredits(): String =
    if (this % 1.0 == 0.0) "${toInt()} CFU" else String.format(Locale.ITALY, "%.1f CFU", this)

/** Esse3 activity types. */
private fun String.activityTypeLabel(): String = when (uppercase()) {
    "LEZ" -> "Lezione"
    "ESE" -> "Esercitazione"
    "LAB" -> "Laboratorio"
    "SEM" -> "Seminario"
    "TIR" -> "Tirocinio"
    else -> this
}

private fun SyllabusSectionKind.title(): String = when (this) {
    SyllabusSectionKind.OBJECTIVES -> "Obiettivi formativi"
    SyllabusSectionKind.SUMMARY -> "Contenuti"
    SyllabusSectionKind.PROGRAM -> "Programma"
    SyllabusSectionKind.PREREQUISITES -> "Prerequisiti"
    SyllabusSectionKind.PROPAEDEUTICS -> "Propedeuticità"
    SyllabusSectionKind.TEACHING_METHODS -> "Metodi didattici"
    SyllabusSectionKind.ASSESSMENT -> "Modalità di verifica"
    SyllabusSectionKind.TEXTBOOKS -> "Testi di riferimento"
    SyllabusSectionKind.WORKLOAD -> "Carico di lavoro"
}
