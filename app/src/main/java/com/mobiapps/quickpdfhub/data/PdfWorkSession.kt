package com.mobiapps.quickpdfhub.data

import android.net.Uri

/**
 * In-memory holder for the current PDF operation's selected URIs.
 * Survives recomposition; cleared when the user starts a new operation.
 * Replaced by Room persistence in Feature 7.
 */
object PdfWorkSession {
    var inputUris: List<Uri> = emptyList()
        private set

    var outputUri: Uri? = null

    fun setInputs(uris: List<Uri>) {
        inputUris = uris
        outputUri = null
    }

    fun clear() {
        inputUris = emptyList()
        outputUri = null
    }

    val primaryInput: Uri? get() = inputUris.firstOrNull()
    val hasInput: Boolean get() = inputUris.isNotEmpty()
}
