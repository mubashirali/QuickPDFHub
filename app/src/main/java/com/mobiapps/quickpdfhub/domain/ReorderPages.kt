package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ReorderPages {

    suspend fun execute(context: Context, inputUri: Uri, newOrder: List<Int>): WorkResult =
        withContext(Dispatchers.IO) {
            var srcDoc: PDDocument? = null
            var newDoc: PDDocument? = null
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                if (newOrder.isEmpty()) {
                    return@withContext WorkResult.Error("No page order provided.")
                }

                val sizeBeforeBytes = context.contentResolver
                    .openFileDescriptor(inputUri, "r")
                    ?.use { it.statSize } ?: 0L

                val inputStream = context.contentResolver.openInputStream(inputUri)
                    ?: return@withContext WorkResult.Error("Cannot open the selected file.")
                srcDoc = PDDocument.load(inputStream)
                inputStream.close()

                val srcPages = srcDoc.pages.toList()
                if (newOrder.any { it < 0 || it >= srcPages.size }) {
                    return@withContext WorkResult.Error("Page order contains invalid page indices.")
                }

                newDoc = PDDocument()
                for (oldIndex in newOrder) newDoc.importPage(srcPages[oldIndex])

                val inputName = displayNameOf(context, inputUri)
                    .removeSuffix(".pdf").removeSuffix(".PDF")
                val outputName = "${inputName}_reordered.pdf"
                val outputFile = File(context.cacheDir, outputName)
                newDoc.save(outputFile)

                WorkResult.Success(
                    outputUri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", outputFile,
                    ),
                    outputFileName = outputName,
                    sizeBeforeBytes = sizeBeforeBytes,
                    sizeAfterBytes = outputFile.length(),
                )
            } catch (e: Throwable) {
                WorkResult.Error(e.message ?: "Page reorder failed.")
            } finally {
                newDoc?.close()
                srcDoc?.close()
            }
        }
}
