package it.fra.myunical.domain.model.taxes

import java.time.LocalDate

/** Charges of every career of the user, merged; [semaphore] refers to the current career. */
data class TaxSummary(
    val semaphore: TaxSemaphore,
    val semaphoreAmountDue: Double?,
    val invoices: List<TaxInvoice>,
    val today: LocalDate
) {
    /** Unpaid charges, nearest due date first. */
    val unpaid: List<TaxInvoice> = invoices
        .filterNot(TaxInvoice::isPaid)
        .sortedWith(compareBy(nullsLast()) { it.dueDate })

    /** Paid charges, most recent first. */
    val paid: List<TaxInvoice> = invoices
        .filter(TaxInvoice::isPaid)
        .sortedWith(
            compareByDescending<TaxInvoice> { it.academicYear ?: 0 }
                .thenByDescending { it.paymentDate ?: it.dueDate }
        )

    val outstandingAmount: Double
        get() = unpaid.sumOf(TaxInvoice::outstandingAmount)
            .takeIf { it > 0.0 }
            ?: semaphoreAmountDue?.takeIf { semaphore != TaxSemaphore.GREEN } ?: 0.0

    val hasOverdue: Boolean
        get() = semaphore == TaxSemaphore.RED || unpaid.any { it.status(today) == TaxStatus.OVERDUE }

    val isUpToDate: Boolean
        get() = unpaid.isEmpty() && semaphore in setOf(TaxSemaphore.GREEN, TaxSemaphore.UNKNOWN)
}
