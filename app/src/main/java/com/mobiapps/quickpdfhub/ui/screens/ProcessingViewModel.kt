package com.mobiapps.quickpdfhub.ui.screens

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.data.RecentFile
import com.mobiapps.quickpdfhub.domain.CompressPdf
import com.mobiapps.quickpdfhub.domain.DeletePages
import com.mobiapps.quickpdfhub.domain.JpgToPdf
import com.mobiapps.quickpdfhub.domain.MergePdf
import com.mobiapps.quickpdfhub.domain.PdfToJpg
import com.mobiapps.quickpdfhub.domain.ReorderPages
import com.mobiapps.quickpdfhub.domain.SplitPdf
import com.mobiapps.quickpdfhub.domain.WorkResult
import com.mobiapps.quickpdfhub.domain.formatBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProcessingViewModel(application: Application) : AndroidViewModel(application) {

    sealed class State {
        object Processing : State()
        data class Done(val result: WorkResult.Success) : State()
        data class Failed(val message: String) : State()
    }

    private val _state = MutableStateFlow<State>(State.Processing)
    val state: StateFlow<State> = _state.asStateFlow()

    private var started = false

    fun start(toolType: String) {
        // Guard prevents re-execution on recomposition. Nav pops the PROCESSING route on
        // success/error, so this ViewModel is not reused if the user navigates back.
        if (started) return
        started = true

        viewModelScope.launch {
            val ctx = getApplication<Application>().applicationContext
            val result = when (toolType) {
                "compress" -> {
                    val uri = PdfWorkSession.primaryInput
                        ?: return@launch run { _state.value = State.Failed("No file selected.") }
                    CompressPdf.execute(ctx, uri, PdfWorkSession.compressionQuality)
                }
                "pdf_to_jpg" -> {
                    val uri = PdfWorkSession.primaryInput
                        ?: return@launch run { _state.value = State.Failed("No file selected.") }
                    PdfToJpg.execute(ctx, uri)
                }
                "jpg_to_pdf" -> {
                    if (PdfWorkSession.inputUris.isEmpty()) {
                        return@launch run { _state.value = State.Failed("No images selected.") }
                    }
                    JpgToPdf.execute(ctx, PdfWorkSession.inputUris)
                }
                "merge" -> {
                    if (PdfWorkSession.inputUris.size < 2) {
                        return@launch run { _state.value = State.Failed("Select at least 2 PDFs to merge.") }
                    }
                    MergePdf.execute(ctx, PdfWorkSession.inputUris)
                }
                "split" -> {
                    val uri = PdfWorkSession.primaryInput
                        ?: return@launch run { _state.value = State.Failed("No file selected.") }
                    SplitPdf.execute(ctx, uri, PdfWorkSession.splitAfterPage)
                }
                "delete" -> {
                    val uri = PdfWorkSession.primaryInput
                        ?: return@launch run { _state.value = State.Failed("No file selected.") }
                    if (PdfWorkSession.pagesToDelete.isEmpty()) {
                        return@launch run { _state.value = State.Failed("No pages selected for deletion.") }
                    }
                    DeletePages.execute(ctx, uri, PdfWorkSession.pagesToDelete)
                }
                "reorder" -> {
                    val uri = PdfWorkSession.primaryInput
                        ?: return@launch run { _state.value = State.Failed("No file selected.") }
                    if (PdfWorkSession.reorderedPageIndices.isEmpty()) {
                        return@launch run { _state.value = State.Failed("No page order specified.") }
                    }
                    ReorderPages.execute(ctx, uri, PdfWorkSession.reorderedPageIndices)
                }
                else -> WorkResult.Error("'$toolType' is not yet implemented.")
            }

            _state.value = when (result) {
                is WorkResult.Success -> {
                    PdfWorkSession.lastResult = result
                    if (toolType == "split" && result.outputUris.size > 1) {
                        // Fix #2: persist both split parts as separate RecentFile entries
                        val part1Uri = result.outputUris[0]
                        val part2Uri = result.outputUris[1]
                        val part1Name = result.outputFileName
                        val part2Name = result.outputFileName.replace("_part1.pdf", "_part2.pdf")
                        val nowMillis = System.currentTimeMillis()
                        val nowDate = PdfWorkSession.formatTimestamp(nowMillis)
                        val part1Size = getUriFileSize(ctx, part1Uri).takeIf { it > 0 } ?: (result.sizeAfterBytes / 2)
                        val part2Size = getUriFileSize(ctx, part2Uri).takeIf { it > 0 } ?: (result.sizeAfterBytes / 2)
                        val entry1 = RecentFile(
                            name = part1Name,
                            operation = operationLabel(toolType),
                            size = part1Size.formatBytes(),
                            date = nowDate,
                            outputUri = part1Uri.toString(),
                        )
                        val entry2 = RecentFile(
                            name = part2Name,
                            operation = operationLabel(toolType),
                            size = part2Size.formatBytes(),
                            date = nowDate,
                            outputUri = part2Uri.toString(),
                        )
                        PdfWorkSession.addRecent(entry1)
                        PdfWorkSession.addRecent(entry2)
                        try { PdfWorkSession.addRecentToRoom(ctx, entry1, nowMillis) } catch (e: CancellationException) { throw e } catch (e: Exception) { /* ignore */ }
                        try { PdfWorkSession.addRecentToRoom(ctx, entry2, nowMillis) } catch (e: CancellationException) { throw e } catch (e: Exception) { /* ignore */ }
                    } else {
                        val nowMillis = System.currentTimeMillis()
                        val firstUri = result.outputUris.firstOrNull() ?: result.outputUri
                        val entry = RecentFile(
                            name = if (result.pageCount > 0) "${result.pageCount} images" else result.outputFileName,
                            operation = operationLabel(toolType),
                            size = result.sizeAfterBytes.formatBytes(),
                            date = PdfWorkSession.formatTimestamp(nowMillis),
                            outputUri = firstUri.toString(),
                        )
                        PdfWorkSession.addRecent(entry)
                        try { PdfWorkSession.addRecentToRoom(ctx, entry, nowMillis) } catch (e: CancellationException) { throw e } catch (e: Exception) { /* ignore */ }
                    }
                    State.Done(result)
                }
                is WorkResult.Error -> State.Failed(result.message)
            }
        }
    }

    private fun getUriFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else -1L
            } ?: -1L
        } catch (_: Exception) { -1L }
    }

    private fun operationLabel(toolType: String): String = when (toolType) {
        "compress" -> "Compressed"
        "merge" -> "Merged"
        "split" -> "Split"
        "delete" -> "Deleted pages"
        "reorder" -> "Reordered"
        "pdf_to_jpg" -> RecentFile.OP_PDF_TO_JPG
        "jpg_to_pdf" -> RecentFile.OP_JPG_TO_PDF
        else -> toolType.replaceFirstChar { it.uppercase() }
    }
}
