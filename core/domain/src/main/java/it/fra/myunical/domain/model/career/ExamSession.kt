package it.fra.myunical.domain.model.career

import java.time.LocalDate
import java.time.LocalTime

/** An exam session ("appello") of a course in the student's libretto. */
data class ExamSession(
    val id: Long,
    /** Libretto row (adsceId) the session belongs to. */
    val courseRowId: Long,
    val courseName: String,
    val description: String,
    val date: LocalDate,
    val time: LocalTime?,
    val enrollStart: LocalDate?,
    val enrollEnd: LocalDate?,
    /** Esse3 reports P when bookings are open. */
    val isBookingOpen: Boolean,
    val president: String?,
    val booking: ExamBooking? = null
) {
    fun bookingState(today: LocalDate): BookingState = when {
        booking != null -> BookingState.BOOKED
        isBookingOpen -> BookingState.OPEN
        enrollStart != null && today.isBefore(enrollStart) -> BookingState.NOT_YET_OPEN
        else -> BookingState.CLOSED
    }
}

enum class BookingState { OPEN, NOT_YET_OPEN, CLOSED, BOOKED }

data class ExamBooking(
    val courseRowId: Long,
    val sessionId: Long,
    /** Id of the student's enrolment, needed to download the booking receipt. */
    val bookingId: Long? = null,
    val courseName: String? = null,
    val date: LocalDate? = null,
    val position: Int?,
    val room: String?,
    val building: String?
)
