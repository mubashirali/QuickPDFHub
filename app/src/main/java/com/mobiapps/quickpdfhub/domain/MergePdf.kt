package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MergePdf {

    suspend fun execute(context: Context, inputUris: List<Uri>): WorkResult =
        withContext(Dispatchers.IO) {
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                val sizeBeforeBytes = inputUris.sumOf { uri ->
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                }

                val firstDisplayName = displayNameOf(context, inputUris.first())
                val baseName = firstDisplayName.removeSuffix(".pdf").removeSuffix(".PDF")
                val outputFileName = "${baseName}_merged.pdf"
                val outputFile = File(context.cacheDir, outputFileName)

                // Source documents must stay open until after save() because importPage()
                // keeps references into the source document's object store.
                val sourceDocs = mutableListOf<PDDocument>()
                try {
                    PDDocument().use { outputDoc ->
                        for (uri in inputUris) {
                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                val srcDoc = PDDocument.load(stream)
                                sourceDocs.add(srcDoc)
                                for (page in srcDoc.pages) outputDoc.importPage(page)
                            }
                        }
                        outputDoc.save(outputFile)
                    }
                } finally {
                    sourceDocs.forEach { runCatching { it.close() } }
                }

                val outputUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    outputFile
                )

                WorkResult.Success(
                    outputUri = outputUri,
                    outputFileName = outputFileName,
                    sizeBeforeBytes = sizeBeforeBytes,
                    sizeAfterBytes = outputFile.length(),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                WorkResult.Error(e.message ?: "Merge failed.")
            }
        }
}
