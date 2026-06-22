package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val TAG = "PdfToJpg"

object PdfToJpg {

    suspend fun execute(context: Context, inputUri: Uri): WorkResult =
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null
            val tempInput = File(context.cacheDir, "pdf_to_jpg_input.pdf")
            try {
                // Copy to a local file so PdfRenderer always gets a seekable descriptor.
                // SAF URIs from cloud providers (Drive, Dropbox) are non-seekable streams.
                val sizeBeforeBytes = context.contentResolver
                    .openInputStream(inputUri)
                    ?.use { src ->
                        FileOutputStream(tempInput).use { dst -> src.copyTo(dst) }
                        tempInput.length()
                    }
                    ?: return@withContext WorkResult.Error("Cannot read the selected file.")

                pfd = ParcelFileDescriptor.open(
                    tempInput, ParcelFileDescriptor.MODE_READ_ONLY,
                )
                renderer = PdfRenderer(pfd)
                val pageCount = renderer.pageCount

                if (pageCount == 0) {
                    return@withContext WorkResult.Error("The PDF has no pages.")
                }

                val inputName = displayNameOf(context, inputUri)
                    .removeSuffix(".pdf").removeSuffix(".PDF")
                    .replace(Regex("[/\\\\:*?\"<>|]"), "_") // strip invalid path chars

                val outputUris = mutableListOf<Uri>()
                var totalSizeAfter = 0L

                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)

                    // ~144 DPI (PDF points are 72 DPI, 2× gives ~144 DPI)
                    val scale = 2f
                    val bitmapW = (page.width * scale).toInt().coerceAtLeast(1)
                    val bitmapH = (page.height * scale).toInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    page.close()

                    val outputFile = File(context.cacheDir, "${inputName}_page${i + 1}.jpg")
                    FileOutputStream(outputFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    bitmap.recycle()

                    totalSizeAfter += outputFile.length()
                    outputUris.add(
                        FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", outputFile,
                        )
                    )
                }

                WorkResult.Success(
                    outputUri = outputUris.first(),
                    outputFileName = "${inputName}_page1.jpg",
                    sizeBeforeBytes = sizeBeforeBytes,
                    sizeAfterBytes = totalSizeAfter,
                    outputUris = outputUris,
                    pageCount = pageCount,
                )
            } catch (e: Throwable) {
                Log.e(TAG, "PDF to JPG failed", e)
                WorkResult.Error(e.message ?: "PDF to JPG conversion failed.")
            } finally {
                renderer?.close() ?: pfd?.close() // pfd closed by renderer; if renderer null, close pfd directly
                tempInput.delete()
            }
        }

}
