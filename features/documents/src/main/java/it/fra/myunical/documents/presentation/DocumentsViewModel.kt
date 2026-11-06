package it.fra.myunical.documents.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.documents.models.DocumentUiModel
import it.fra.myunical.documents.models.DocumentsEvent
import it.fra.myunical.documents.models.DocumentsUiState
import it.fra.myunical.documents.storage.DocumentStore
import it.fra.myunical.domain.model.documents.DocumentSource
import it.fra.myunical.domain.model.documents.DocumentUnavailableException
import it.fra.myunical.domain.model.documents.StudentDocument
import it.fra.myunical.domain.usecase.DownloadDocumentUseCase
import it.fra.myunical.domain.usecase.GetStudentDocumentsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class DocumentsViewModel(
    private val getStudentDocuments: GetStudentDocumentsUseCase,
    private val downloadDocument: DownloadDocumentUseCase,
    private val documentStore: DocumentStore
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentsUiState())
    val state: StateFlow<DocumentsUiState> = _state.asStateFlow()

    private val _events = Channel<DocumentsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            getStudentDocuments().collect { documents ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        bookingReceipts = documents.bookingReceipts?.map { doc -> doc.toUiModel() },
                        paymentReceipts = documents.paymentReceipts?.map { doc -> doc.toUiModel() }
                    )
                }
            }
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        getStudentDocuments.refresh()
    }

    fun download(item: DocumentUiModel) {
        if (item.id in _state.value.downloading) return
        _state.update { it.copy(downloading = it.downloading + item.id) }
        viewModelScope.launch {
            try {
                val bytes = downloadDocument(item.document.source)
                val uri = documentStore.save("${item.id}.pdf", bytes)
                _events.send(DocumentsEvent.Open(uri, item.title))
            } catch (e: CancellationException) {
                throw e
            } catch (e: DocumentUnavailableException) {
                _events.send(DocumentsEvent.Error("Esse3 non rende disponibile questo documento"))
            } catch (e: Exception) {
                _events.send(DocumentsEvent.Error("Download non riuscito, controlla la connessione"))
            } finally {
                _state.update { it.copy(downloading = it.downloading - item.id) }
            }
        }
    }

    private fun StudentDocument.toUiModel(): DocumentUiModel {
        val dateLabel = date?.format(DATE_FORMAT)
        val subtitle = when (source) {
            is DocumentSource.BookingReceipt -> listOfNotNull(dateLabel?.let { "Appello del $it" })
            is DocumentSource.PaymentReceipt -> listOfNotNull(amount?.let(CURRENCY::format), dateLabel?.let { "pagato il $it" })
        }.joinToString(" · ")
        return DocumentUiModel(id = id, title = title, subtitle = subtitle, document = this)
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val CURRENCY: NumberFormat = NumberFormat.getCurrencyInstance(Locale.ITALY)
    }
}
