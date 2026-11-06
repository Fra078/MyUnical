package it.fra.myunical.domain.model.career

import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.Course
import java.time.LocalDate

data class CareerData(
    val career: CareerTrackModel,
    val courses: List<Course>,
    val overview: CareerOverview,
    /** Upcoming sessions, nearest first; null when Esse3 could not provide them. */
    val upcomingSessions: List<ExamSession>?,
    /** Current exam bookings; null when Esse3 could not provide them. */
    val bookings: List<ExamBooking>? = null,
    val today: LocalDate
) {
    val openSessions: List<ExamSession>
        get() = upcomingSessions.orEmpty().filter { it.bookingState(today) == BookingState.OPEN }
}
