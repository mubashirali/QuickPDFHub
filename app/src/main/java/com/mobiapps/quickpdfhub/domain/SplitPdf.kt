package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object SplitPdf {

    suspend fun execute(context: Context, inputUri: Uri, splitAfterPage: Int): WorkResult =
        withContext(Dispatchers.IO) {
            var doc: PDDocument? = null
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                val sizeBeforeBytes = context.contentResolver
                    .openFileDescriptor(inputUri, "r")
                    ?.use { it.statSize } ?: 0L

                val inputStream = context.contentResolver.openInputStream(inputUri)
                    ?: return@withContext WorkResult.Error("Cannot open the selected file.")
                doc = PDDocument.load(inputStream)
                inputStream.close()

                val totalPages = doc.numberOfPages
                if (splitAfterPage < 1 || splitAfterPage >= totalPages) {
                    return@withContext WorkResult.Error(
                        "Invalid split point. Must be between 1 and ${totalPages - 1}."
                    )
                }

                val inputName = displayNameOf(context, inputUri)
                    .removeSuffix(".pdf").removeSuffix(".PDF")
                val outputFile1 = File(context.cacheDir, "${inputName}_part1.pdf")
                val outputFile2 = File(context.cacheDir, "${inputName}_part2.pdf")

                val pages = doc.pages.toList()
                val part1 = PDDocument()
                val part2 = PDDocument()
                try {
                    for (i in 0 until splitAfterPage) part1.importPage(pages[i])
                    for (i in splitAfterPage until totalPages) part2.importPage(pages[i])
                    part1.save(outputFile1)
                    part2.save(outputFile2)
                } finally {
                    part1.close()
                    part2.close()
                }

                val uri1 = FileProvider.getUriForFile(
                    context, "${context.packageName}.provider", outputFile1,
                )
                val uri2 = FileProvider.getUriForFile(
                    context, "${context.packageName}.provider", outputFile2,
                )

                WorkResult.Success(
                    outputUri = uri1,
                    outputFileName = "${inputName}_part1.pdf",
                    sizeBeforeBytes = sizeBeforeBytes,
                    sizeAfterBytes = outputFile1.length() + outputFile2.length(),
                    outputUris = listOf(uri1, uri2),
                )
            } catch (e: Throwable) {
                WorkResult.Error(e.message ?: "Split failed.")
            } finally {
                doc?.close()
            }
        }
}
