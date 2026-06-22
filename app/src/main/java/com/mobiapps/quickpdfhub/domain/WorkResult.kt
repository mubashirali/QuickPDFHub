package com.mobiapps.quickpdfhub.domain

import android.net.Uri

sealed class WorkResult {
    data class Success(
        val outputUri: Uri,
        val outputFileName: String,
        val sizeBeforeBytes: Long,
        val sizeAfterBytes: Long,
        val outputUris: List<Uri> = emptyList(), // non-empty for multi-file output (e.g. PDF→JPG)
        val pageCount: Int = 0,                  // > 0 for PDF→JPG results
    ) : WorkResult()

    data class Error(val message: String) : WorkResult()
}

fun Long.formatBytes(): String = when {
    this >= 1_048_576 -> "%.1f MB".format(this / 1_048_576.0)
    this >= 1_024 -> "%.1f KB".format(this / 1_024.0)
    else -> "$this B"
}
