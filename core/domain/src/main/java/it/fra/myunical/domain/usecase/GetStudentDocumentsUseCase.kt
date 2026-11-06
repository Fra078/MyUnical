package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.career.CareerData
import it.fra.myunical.domain.model.documents.DocumentSource
import it.fra.myunical.domain.model.documents.StudentDocument
import it.fra.myunical.domain.model.documents.StudentDocuments
import it.fra.myunical.domain.model.taxes.TaxSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * PDFs Esse3 can generate for the student: receipts of the current exam bookings and
 * pagoPA receipts of paid taxes. Each list is null when its source failed to load.
 */
class GetStudentDocumentsUseCase(
    private val getCareerData: GetCareerDataUseCase,
    private val getTaxSummary: GetTaxSummaryUseCase
) {

    operator fun invoke(): Flow<StudentDocuments> = combine(getCareerData(), getTaxSummary()) { career, taxes ->
        StudentDocuments(
            bookingReceipts = career.getOrNull()?.bookingReceipts(),
            paymentReceipts = taxes.getOrNull()?.paymentReceipts()
        )
    }

    fun refresh() {
        getCareerData.refresh()
        getTaxSummary.refresh()
    }

    private fun CareerData.bookingReceipts(): List<StudentDocument>? = bookings?.mapNotNull { booking ->
        val bookingId = booking.bookingId ?: return@mapNotNull null
        StudentDocument(
            id = "booking-$bookingId",
            title = booking.courseName
                ?: courses.firstOrNull { it.id == booking.courseRowId }?.name
                ?: "Prenotazione esame",
            date = booking.date,
            source = DocumentSource.BookingReceipt(career.matId, booking.courseRowId, bookingId)
        )
    }?.sortedBy { it.date }

    // Only invoiced charges have a pagoPA receipt
    private fun TaxSummary.paymentReceipts(): List<StudentDocument> = paid.mapNotNull { invoice ->
        val invoiceId = invoice.invoiceId ?: return@mapNotNull null
        StudentDocument(
            id = "payment-${invoice.id}",
            title = invoice.description,
            date = invoice.paymentDate,
            amount = invoice.paidAmount ?: invoice.amount,
            source = DocumentSource.PaymentReceipt(invoiceId)
        )
    }
}
