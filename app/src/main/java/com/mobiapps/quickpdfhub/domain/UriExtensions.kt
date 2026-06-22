package com.mobiapps.quickpdfhub.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast

internal fun displayNameOf(context: Context, uri: Uri): String =
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && idx >= 0) cursor.getString(idx) else null
    } ?: uri.lastPathSegment ?: "document.pdf"

fun openRecentFile(context: Context, uriString: String?, fileName: String, isImage: Boolean = false) {
    if (uriString == null) {
        Toast.makeText(context, "File not available", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = Uri.parse(uriString)
    val mimeType = context.contentResolver.getType(uri)
        ?.takeIf { it.isNotEmpty() }
        ?: if (
            isImage ||
            fileName.endsWith(".jpg", ignoreCase = true) ||
            fileName.endsWith(".jpeg", ignoreCase = true)
        ) "image/jpeg" else "application/pdf"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show()
    }
}
