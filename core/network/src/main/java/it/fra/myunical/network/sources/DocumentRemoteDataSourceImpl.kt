package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import it.fra.myunical.domain.model.documents.DocumentSource
import it.fra.myunical.domain.model.documents.DocumentUnavailableException
import it.fra.myunical.domain.sources.DocumentRemoteDataSource

class DocumentRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : DocumentRemoteDataSource {

    override suspend fun download(source: DocumentSource): ByteArray {
        val path = when (source) {
            is DocumentSource.BookingReceipt ->
                "libretto-service-v2/libretti/${source.matId}/righe/${source.courseRowId}" +
                    "/prenotazioni/${source.bookingId}/statino-prenotazione"
            is DocumentSource.PaymentReceipt ->
                "tasse-service-v1/pagopa/quietanza/${source.invoiceId}/lingua/it"
        }
        // The endpoints only produce octet-stream: the default JSON Accept header gets a 406
        val response = httpClient.get(path) { accept(ContentType.Application.OctetStream) }
        when {
            response.status.isSuccess() -> Unit
            // Esse3 answers 422 (or 404) when it can't generate the PDF for this record
            response.status == HttpStatusCode.UnprocessableEntity || response.status == HttpStatusCode.NotFound ->
                throw DocumentUnavailableException("Documento non disponibile: ${response.status}")
            else -> throw IllegalStateException("Errore nella richiesta: ${response.status}")
        }
        val bytes = response.readRawBytes()
        // Guard against an HTML or JSON error page served with 200
        if (!bytes.startsWithPdfHeader()) throw DocumentUnavailableException("La risposta non è un PDF")
        return bytes
    }

    private fun ByteArray.startsWithPdfHeader() =
        size > 4 && this[0] == '%'.code.toByte() && this[1] == 'P'.code.toByte() &&
            this[2] == 'D'.code.toByte() && this[3] == 'F'.code.toByte()
}
