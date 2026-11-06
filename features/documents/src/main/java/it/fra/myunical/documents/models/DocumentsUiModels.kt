package it.fra.myunical.documents.models

import android.net.Uri
import it.fra.myunical.domain.model.documents.StudentDocument

data class DocumentsUiState(
    val isLoading: Boolean = true,
    /** Null when the source could not be loaded. */
    val bookingReceipts: List<DocumentUiModel>? = null,
    val paymentReceipts: List<DocumentUiModel>? = null,
    val downloading: Set<String> = emptySet()
)

data class DocumentUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val document: StudentDocument
)

sealed interface DocumentsEvent {
    data class Open(val uri: Uri, val title: String) : DocumentsEvent
    data class Error(val message: String) : DocumentsEvent
}
