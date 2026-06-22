package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DeletePages {

    suspend fun execute(context: Context, inputUri: Uri, pagesToDelete: List<Int>): WorkResult =
        withContext(Dispatchers.IO) {
            var doc: PDDocument? = null
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                if (pagesToDelete.isEmpty()) {
                    return@withContext WorkResult.Error("No pages selected for deletion.")
                }

                val sizeBeforeBytes = context.contentResolver
                    .openFileDescriptor(inputUri, "r")
                    ?.use { it.statSize } ?: 0L

                val inputStream = context.contentResolver.openInputStream(inputUri)
                    ?: return@withContext WorkResult.Error("Cannot open the selected file.")
                doc = PDDocument.load(inputStream)
                inputStream.close()

                if (pagesToDelete.size >= doc.numberOfPages) {
                    return@withContext WorkResult.Error("Cannot delete all pages from the document.")
                }

                pagesToDelete.sortedDescending().forEach { doc!!.removePage(it) }

                val inputName = displayNameOf(context, inputUri)
                    .removeSuffix(".pdf").removeSuffix(".PDF")
                val outputName = "${inputName}_deleted.pdf"
                val outputFile = File(context.cacheDir, outputName)
                doc.save(outputFile)

                WorkResult.Success(
                    outputUri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", outputFile,
                    ),
                    outputFileName = outputName,
                    sizeBeforeBytes = sizeBeforeBytes,
                    sizeAfterBytes = outputFile.length(),
                )
            } catch (e: Throwable) {
                WorkResult.Error(e.message ?: "Page deletion failed.")
            } finally {
                doc?.close()
            }
        }
}
