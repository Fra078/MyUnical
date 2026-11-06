package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.career.CareerOverview
import it.fra.myunical.domain.model.career.ExamBooking
import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.domain.model.general.Course

interface CinecaRemoteDataSource {

    fun getUserPhotoEndpoint(persId: Int) : String

    suspend fun fetchStudyPlan(matId: Int): Result<List<Course>>

    /** Credits and official averages of the libretto [matId]. Throws on errors. */
    suspend fun fetchCareerOverview(matId: Int): CareerOverview

    /** Every session (past ones included) of the courses in the libretto [matId]. Throws on errors. */
    suspend fun fetchExamSessions(matId: Int): List<ExamSession>

    /** Active bookings of the libretto [matId]. Throws on errors. */
    suspend fun fetchBookings(matId: Int): List<ExamBooking>
}
