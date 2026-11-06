package it.fra.myunical.today.mapper

import it.fra.myunical.domain.model.schedule.NextLesson
import it.fra.myunical.today.models.CommitmentType
import it.fra.myunical.today.models.UpcomingCommitment
import it.fra.myunical.today.models.UpcomingCommitmentState
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ItalianLocale = Locale.ITALIAN
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DayFormatter = DateTimeFormatter.ofPattern("EEEE d MMM", ItalianLocale)

fun NextLesson.toUiState(now: LocalDateTime): UpcomingCommitmentState = when (this) {
    NextLesson.NotConfigured -> UpcomingCommitmentState.NotConfigured
    NextLesson.NoneScheduled -> UpcomingCommitmentState.NothingScheduled
    NextLesson.Unavailable -> UpcomingCommitmentState.Unavailable
    is NextLesson.Upcoming -> UpcomingCommitmentState.Available(toUiModel(now))
}

private fun NextLesson.Upcoming.toUiModel(now: LocalDateTime): UpcomingCommitment {
    val start = LocalDateTime.of(lesson.date, lesson.start)
    val end = LocalDateTime.of(lesson.date, lesson.end)
    val isOngoing = !now.isBefore(start)
    return UpcomingCommitment(
        title = lesson.title.toDisplayName(),
        type = CommitmentType.LESSON,
        timeSlot = "${lesson.start.format(TimeFormatter)} – ${lesson.end.format(TimeFormatter)}",
        room = lesson.rooms.joinToString(" · ") { room ->
            listOfNotNull(room.name, room.building?.takeIf(String::isNotBlank)).joinToString(", ")
        }.ifBlank { null },
        relativeTime = if (isOngoing) "Termina tra ${Duration.between(now, end).toDisplayString()}"
        else relativeStart(now, start),
        teachers = lesson.teachers.joinToString(", ") { it.toDisplayName() }.ifBlank { null },
        scheduleName = providerName.ifBlank { null },
        progress = if (isOngoing) {
            val total = Duration.between(start, end).seconds.coerceAtLeast(1)
            (Duration.between(start, now).seconds.toFloat() / total).coerceIn(0f, 1f)
        } else null,
        followedBy = following?.let { "Poi alle ${it.start.format(TimeFormatter)} · ${it.title.toDisplayName()}" }
    )
}

private fun relativeStart(now: LocalDateTime, start: LocalDateTime): String {
    val days = Duration.between(now.toLocalDate().atStartOfDay(), start.toLocalDate().atStartOfDay()).toDays()
    return when (days) {
        0L -> "Tra ${Duration.between(now, start).toDisplayString()}"
        1L -> "Domani"
        else -> start.format(DayFormatter).replaceFirstChar { it.titlecase(ItalianLocale) }
    }
}

private fun Duration.toDisplayString(): String {
    // Round up so that a lesson starting in 30 seconds reads "1 min" rather than "0 min"
    val totalMinutes = (seconds + 59) / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0L -> "$minutes min"
        minutes == 0L -> "$hours h"
        else -> "$hours h $minutes min"
    }
}

/** The university APIs return most names in upper case: "INGEGNERIA DEL SOFTWARE" -> "Ingegneria Del Software". */
private fun String.toDisplayName(): String {
    if (any(Char::isLowerCase)) return this
    return lowercase(ItalianLocale).split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase(ItalianLocale) }
    }
}
