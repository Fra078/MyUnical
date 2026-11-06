package it.fra.myunical.documents.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Keeps downloaded PDFs in the cache, so the system can clear them when space runs low. */
class DocumentStore(private val context: Context) {

    private val directory get() = File(context.cacheDir, "documents").apply { mkdirs() }

    /** Writes [bytes] as [fileName] and returns a content Uri other apps can read. */
    suspend fun save(fileName: String, bytes: ByteArray): Uri = withContext(Dispatchers.IO) {
        val file = File(directory, fileName.sanitized())
        file.writeBytes(bytes)
        FileProvider.getUriForFile(context, "${context.packageName}.documents", file)
    }

    private fun String.sanitized() = replace(Regex("[^A-Za-z0-9._-]"), "_")
}
