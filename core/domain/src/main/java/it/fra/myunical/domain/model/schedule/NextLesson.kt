package it.fra.myunical.domain.model.schedule

sealed interface NextLesson {
    /** No schedule has been configured yet. */
    data object NotConfigured : NextLesson

    /** Nothing scheduled in the look-ahead window (e.g. holidays). */
    data object NoneScheduled : NextLesson

    data object Unavailable : NextLesson

    data class Upcoming(
        val lesson: ScheduleEvent,
        val providerName: String,
        /** The lesson after [lesson] on the same day, if any. */
        val following: ScheduleEvent?
    ) : NextLesson
}
