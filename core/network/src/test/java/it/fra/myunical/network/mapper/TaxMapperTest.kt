package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.taxes.TaxSemaphore
import it.fra.myunical.network.models.taxes.StudentChargeDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Based on a real (anonymized) `addebiti-studente` response, plus the quirks of the published spec. */
class TaxMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val rows: List<StudentChargeDto> = json.decodeFromString(
        checkNotNull(javaClass.classLoader?.getResource("taxes/addebiti-studente.json")).readText()
    )

    private val taxes = rows.toStudentTaxes(stuId = 1)

    @Test
    fun `groups fee items by invoice and drops cancelled rows`() {
        // 5 real rows = 3 invoices, + 1 unpaid invoice; the cancelled one is dropped
        assertEquals(4, taxes.invoices.size)
        val pergamena = taxes.invoices.single { it.id == "F2457573" }
        assertEquals(listOf("Tassa di rilascio pergamena", "Bollo virtuale"), pergamena.items.map { it.description })
        assertEquals("TASSA DI RILASCIO PERGAMENA DI LAUREA", pergamena.description)
        assertEquals(50.0, pergamena.amount, 0.0)
        assertTrue(pergamena.isPaid)
        assertEquals(LocalDate.of(2026, 3, 4), pergamena.paymentDate)
        // Empty due date in the response
        assertNull(pergamena.dueDate)
    }

    @Test
    fun `parses semaphore, dates and comma separated amounts`() {
        assertEquals(TaxSemaphore.GREEN, taxes.semaphore)
        val unpaid = taxes.invoices.single { it.id == "F999" }
        assertFalse(unpaid.isPaid)
        assertEquals(148.5, unpaid.amount, 0.0)
        assertEquals(148.5, unpaid.outstandingAmount, 0.0)
        assertEquals(LocalDate.of(2026, 11, 30), unpaid.dueDate)
        assertEquals("2 di 4", unpaid.installment)
        assertNull(unpaid.paymentDate)
        assertEquals("Contributo iscrizione tardiva", unpaid.description)
    }
}
