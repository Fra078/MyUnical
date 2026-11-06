package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.UserInfoModel
import it.fra.myunical.domain.model.taxes.StudentTaxes
import it.fra.myunical.domain.model.taxes.TaxInvoice
import it.fra.myunical.domain.model.taxes.TaxSemaphore
import it.fra.myunical.domain.model.taxes.TaxStatus
import it.fra.myunical.domain.repository.UserPreferenceRepository
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.TaxRemoteDataSource
import it.fra.myunical.domain.sources.UserInfoDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class GetTaxSummaryUseCaseTest {

    private val today = LocalDate.of(2026, 11, 6)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    private fun invoice(id: String, year: Int, paid: Boolean, due: LocalDate?, amount: Double = 100.0) = TaxInvoice(
        id = id, academicYear = year, description = id, installment = null, dueDate = due,
        amount = amount, isPaid = paid, paidAmount = if (paid) amount else null,
        paymentDate = null, pagoPaNoticeCode = null, items = emptyList()
    )

    private class FakeTaxSource(val byStuId: Map<Int, StudentTaxes>, val failing: Set<Int> = emptySet()) : TaxRemoteDataSource {
        var calls = 0
        override suspend fun getStudentTaxes(stuId: Int): StudentTaxes {
            calls++
            if (stuId in failing) error("HTTP 500")
            return byStuId.getValue(stuId)
        }
    }

    private fun useCase(source: TaxRemoteDataSource): GetTaxSummaryUseCase {
        val careers = listOf(
            CareerTrackModel("Magistrale", matId = 10, stuId = 1, matricola = "1", active = true, closedReason = null),
            CareerTrackModel("Triennale", matId = 20, stuId = 2, matricola = "2", active = false, closedReason = "X")
        )
        val auth = object : AuthLocalDataSource {
            override suspend fun saveCredentials(credentials: UserCredentials) = Unit
            override suspend fun clearCredentials() = Unit
            override suspend fun getCredentials(): UserCredentials? = null
            override fun observeUsername(): Flow<String?> = flowOf("user")
        }
        val userInfo = object : UserInfoDataSource {
            override suspend fun saveUserInfo(userInfo: UserInfoModel) = Unit
            override fun getUserInfo(username: String) = flowOf(UserInfoModel(username, "A", "B", 1, careers))
        }
        val preferences = object : UserPreferenceRepository {
            override suspend fun setCareerId(value: Int) = Unit
            override fun getCareerId(): Flow<Int> = flowOf(10)
        }
        val getUserInfo = GetCurrentUserInfoUseCase(auth, userInfo)
        return GetTaxSummaryUseCase(getUserInfo, GetCurrentCareerUseCase(preferences, getUserInfo), source, clock)
    }

    @Test
    fun `merges every career and takes the semaphore from the current one`() = runBlocking {
        val source = FakeTaxSource(
            mapOf(
                1 to StudentTaxes(1, TaxSemaphore.YELLOW, 50.0, listOf(invoice("F1", 2026, paid = false, due = today.plusDays(10)))),
                2 to StudentTaxes(2, TaxSemaphore.GREEN, 0.0, listOf(invoice("F2", 2024, paid = true, due = null)))
            )
        )
        val summary = useCase(source)().first().getOrThrow()

        assertEquals(TaxSemaphore.YELLOW, summary.semaphore)
        assertEquals(listOf("F1"), summary.unpaid.map { it.id })
        assertEquals(listOf("F2"), summary.paid.map { it.id })
        assertEquals(100.0, summary.outstandingAmount, 0.0)
        assertFalse(summary.isUpToDate)
        assertFalse(summary.hasOverdue)
    }

    @Test
    fun `an unpaid invoice past its due date is overdue`() = runBlocking {
        val source = FakeTaxSource(
            mapOf(
                1 to StudentTaxes(1, TaxSemaphore.GREEN, 0.0, listOf(invoice("F1", 2026, paid = false, due = today.minusDays(1)))),
                2 to StudentTaxes(2, TaxSemaphore.GREEN, 0.0, emptyList())
            )
        )
        val summary = useCase(source)().first().getOrThrow()

        assertEquals(TaxStatus.OVERDUE, summary.unpaid.single().status(today))
        assertTrue(summary.hasOverdue)
    }

    @Test
    fun `old careers failing do not hide the current one, and results are cached`() = runBlocking {
        val source = FakeTaxSource(
            mapOf(1 to StudentTaxes(1, TaxSemaphore.GREEN, 0.0, listOf(invoice("F1", 2026, paid = true, due = null)))),
            failing = setOf(2)
        )
        val useCase = useCase(source)
        val summary = useCase().first().getOrThrow()
        assertTrue(summary.isUpToDate)
        assertEquals(1, summary.paid.size)

        val callsAfterFirstLoad = source.calls
        useCase().first()
        // Only the failed career is retried
        assertEquals(callsAfterFirstLoad + 1, source.calls)
    }

    @Test
    fun `a failing current career is reported as an error`() = runBlocking {
        val source = FakeTaxSource(emptyMap(), failing = setOf(1, 2))
        assertTrue(useCase(source)().first().isFailure)
    }
}
