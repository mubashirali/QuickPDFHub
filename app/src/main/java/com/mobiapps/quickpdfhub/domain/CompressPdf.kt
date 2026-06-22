package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CompressPdf {

    // quality int from PdfWorkSession: 90 = Low compression, 65 = Medium, 35 = High
    suspend fun execute(context: Context, inputUri: Uri, quality: Int): WorkResult =
        withContext(Dispatchers.IO) {
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                val sizeBeforeBytes = context.contentResolver
                    .openFileDescriptor(inputUri, "r")
                    ?.use { it.statSize } ?: 0L

                val inputStream = context.contentResolver.openInputStream(inputUri)
                    ?: return@withContext WorkResult.Error("Cannot open the selected file.")

                val doc = PDDocument.load(inputStream)
                inputStream.close()

                val jpegQuality = quality.toFloat() / 100f

                // Re-encode embedded images on every page; text and vectors are untouched
                for (page in doc.pages) {
                    recompressImages(doc, page.resources, jpegQuality)
                }

                val inputName = displayNameOf(context, inputUri)
                    .removeSuffix(".pdf").removeSuffix(".PDF")
                val outputName = "${inputName}_compressed.pdf"
                val outputFile = File(context.cacheDir, outputName)

                doc.save(outputFile)
                doc.close()

                // If the PDF had no images or was already optimal, output may be same size or larger.
                // In that case return the original rather than a larger file.
                if (outputFile.length() >= sizeBeforeBytes) {
                    outputFile.delete()
                    val originalCopy = File(context.cacheDir, "$inputName.pdf")
                    context.contentResolver.openInputStream(inputUri)?.use { src ->
                        FileOutputStream(originalCopy).use { dst -> src.copyTo(dst) }
                    }
                    return@withContext WorkResult.Success(
                        outputUri = FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", originalCopy,
                        ),
                        outputFileName = "$inputName.pdf",
                        sizeBeforeBytes = sizeBeforeBytes,
                        sizeAfterBytes = originalCopy.length(),
                    )
                }

                WorkResult.Success(
                    outputUri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", outputFile,
                    ),
                    outputFileName = outputName,
                    sizeBeforeBytes = sizeBeforeBytes,
                    sizeAfterBytes = outputFile.length(),
                )
            } catch (e: Throwable) {
                WorkResult.Error(e.message ?: "Compression failed.")
            }
        }

    private fun recompressImages(doc: PDDocument, resources: PDResources?, jpegQuality: Float) {
        resources ?: return
        for (name in resources.xObjectNames) {
            val xObject = try {
                resources.getXObject(name)
            } catch (e: Exception) {
                continue
            }
            if (xObject is PDImageXObject) {
                try {
                    val bitmap = xObject.image ?: continue
                    val recompressed = JPEGFactory.createFromImage(doc, bitmap, jpegQuality)
                    resources.put(name, recompressed)
                    bitmap.recycle()
                } catch (e: Exception) {
                    // Skip images that can't be re-encoded (masks, indexed colour, etc.)
                }
            }
        }
    }

}
