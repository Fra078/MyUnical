package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.documents.DocumentSource
import it.fra.myunical.domain.sources.DocumentRemoteDataSource

/**
 * Downloads the PDF of a document.
 * Throws [it.fra.myunical.domain.model.documents.DocumentUnavailableException] when Esse3 can't generate it.
 */
class DownloadDocumentUseCase(
    private val documentRemoteDataSource: DocumentRemoteDataSource
) {
    suspend operator fun invoke(source: DocumentSource): ByteArray = documentRemoteDataSource.download(source)
}
