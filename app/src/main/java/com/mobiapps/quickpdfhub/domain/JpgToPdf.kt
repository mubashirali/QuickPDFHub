package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object JpgToPdf {

    suspend fun execute(context: Context, inputUris: List<Uri>): WorkResult =
        withContext(Dispatchers.IO) {
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                val sizeBeforeBytes = inputUris.sumOf { uri ->
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                }

                val firstDisplayName = displayNameOf(context, inputUris.first())
                val baseName = firstDisplayName.substringBeforeLast('.')
                val outputFileName = "${baseName}_to_pdf.pdf"
                val outputFile = File(context.cacheDir, outputFileName)

                PDDocument().use { doc ->
                    for (uri in inputUris) {
                        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        } ?: continue

                        val image = JPEGFactory.createFromImage(doc, bitmap, 0.9f)
                        val pageRect = PDRectangle(image.width.toFloat(), image.height.toFloat())
                        val page = PDPage(pageRect)
                        doc.addPage(page)

                        PDPageContentStream(doc, page).use { cs ->
                            cs.drawImage(image, 0f, 0f, image.width.toFloat(), image.height.toFloat())
                        }

                        bitmap.recycle()
                    }

                    doc.save(outputFile)
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
            } catch (e: Throwable) {
                WorkResult.Error(e.message ?: "JPG to PDF conversion failed.")
            }
        }

}
