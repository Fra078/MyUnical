package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.documents.DocumentSource

interface DocumentRemoteDataSource {
    /**
     * Downloads the PDF of a document.
     * Throws [it.fra.myunical.domain.model.documents.DocumentUnavailableException] when Esse3 cannot generate it.
     */
    suspend fun download(source: DocumentSource): ByteArray
}
