package it.fra.myunical.domain.model.taxes

import java.time.LocalDate

/**
 * A single charge issued to the student (an Esse3 "fattura"), with the fee items it is made of.
 * Charges that have not been invoiced yet are reported as standalone invoices too.
 */
data class TaxInvoice(
    val id: String,
    val academicYear: Int?,
    val description: String,
    val installment: String?,
    val dueDate: LocalDate?,
    val amount: Double,
    val isPaid: Boolean,
    val paidAmount: Double?,
    val paymentDate: LocalDate?,
    val pagoPaNoticeCode: String?,
    val items: List<TaxItem>,
    /** Esse3 invoice id ("fattId"); null for charges not invoiced yet. */
    val invoiceId: Long? = null
) {
    /** Amount still to be paid; a partial payment is subtracted from the total. */
    val outstandingAmount: Double
        get() = if (isPaid) 0.0 else (amount - (paidAmount ?: 0.0)).coerceAtLeast(0.0)

    fun status(today: LocalDate): TaxStatus = when {
        isPaid -> TaxStatus.PAID
        dueDate != null && dueDate.isBefore(today) -> TaxStatus.OVERDUE
        else -> TaxStatus.DUE
    }
}

data class TaxItem(
    val description: String,
    val amount: Double
)

enum class TaxStatus { PAID, DUE, OVERDUE }

/** Esse3 "semaforo tasse": RED when blocking fees are overdue, YELLOW when late fees apply. */
enum class TaxSemaphore { GREEN, YELLOW, RED, UNKNOWN }

/** Charges of one career (stuId), as returned by Esse3. */
data class StudentTaxes(
    val stuId: Int,
    val semaphore: TaxSemaphore,
    /** Amount needed to bring the semaphore back to GREEN, as computed by Esse3. */
    val amountDue: Double?,
    val invoices: List<TaxInvoice>
)
