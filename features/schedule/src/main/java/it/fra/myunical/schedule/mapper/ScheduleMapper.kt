package it.fra.myunical.schedule.mapper

import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.schedule.models.LessonUiModel
import java.util.Locale

fun ScheduleEvent.toUiModel(providerName: String?) = LessonUiModel(
    key = "$providerId|$courseCode|$date|$start|$end",
    providerName = providerName,
    courseCode = courseCode,
    title = title.toDisplayName(),
    date = date,
    start = start,
    end = end,
    location = rooms.joinToString(" · ") { room ->
        listOfNotNull(room.name, room.building?.takeIf(String::isNotBlank)).joinToString(", ")
    }.ifBlank { null },
    teachers = teachers.joinToString(", ") { it.toDisplayName() }.ifBlank { null }
)

/** The university APIs return most names in upper case: "INGEGNERIA INFORMATICA" -> "Ingegneria Informatica". */
fun String.toDisplayName(): String {
    if (any(Char::isLowerCase)) return this
    return lowercase(Locale.ITALIAN).split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }
}
