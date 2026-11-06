package it.fra.myunical.domain.model.documents

import java.time.LocalDate

/** A PDF the student can download from Esse3. */
data class StudentDocument(
    val id: String,
    val title: String,
    val date: LocalDate?,
    /** Amount paid, for payment receipts. */
    val amount: Double? = null,
    val source: DocumentSource
)

/** Where a document is generated; each case maps to one Esse3 endpoint. */
sealed interface DocumentSource {
    /** Receipt of an exam booking ("statino di prenotazione"). */
    data class BookingReceipt(val matId: Int, val courseRowId: Long, val bookingId: Long) : DocumentSource

    /** pagoPA receipt of a paid tax. */
    data class PaymentReceipt(val invoiceId: Long) : DocumentSource
}

data class StudentDocuments(
    /** Null when the bookings could not be loaded. */
    val bookingReceipts: List<StudentDocument>?,
    /** Null when the taxes could not be loaded. */
    val paymentReceipts: List<StudentDocument>?
)

/** Esse3 did not generate the document (e.g. a payment made outside pagoPA). */
class DocumentUnavailableException(message: String? = null) : Exception(message)
