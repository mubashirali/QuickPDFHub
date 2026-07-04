package com.mobiapps.quickpdfhub.navigation

import androidx.annotation.StringRes
import com.mobiapps.quickpdfhub.R

object Route {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val TOOL_ENTRY = "tool_entry/{toolType}"
    const val PAGE_THUMBNAIL = "page_thumbnail/{toolType}"
    const val PROCESSING = "processing/{toolType}"
    const val RESULT = "result/{toolType}"
    const val RECENT_FILES = "recent_files?searchActive={searchActive}"
    const val SETTINGS = "settings"
    const val UPGRADE = "upgrade"
    const val PERMISSION_RATIONALE = "permission_rationale"
    const val ERROR = "error"

    fun toolEntry(toolType: String) = "tool_entry/$toolType"
    fun pageThumbnail(toolType: String) = "page_thumbnail/$toolType"
    fun processing(toolType: String) = "processing/$toolType"
    fun result(toolType: String) = "result/$toolType"
    fun recentFiles(searchActive: Boolean = false) = "recent_files?searchActive=$searchActive"
}

enum class ToolType(
    @StringRes val labelRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val ctaLabelRes: Int,
) {
    MERGE(R.string.tool_merge_label, R.string.tool_merge_subtitle, R.string.tool_merge_cta),
    SPLIT(R.string.tool_split_label, R.string.tool_split_subtitle, R.string.tool_split_cta),
    COMPRESS(R.string.tool_compress_label, R.string.tool_compress_subtitle, R.string.tool_compress_cta),
    PDF_TO_JPG(R.string.tool_pdf_to_jpg_label, R.string.tool_pdf_to_jpg_subtitle, R.string.tool_pdf_to_jpg_cta),
    JPG_TO_PDF(R.string.tool_jpg_to_pdf_label, R.string.tool_jpg_to_pdf_subtitle, R.string.tool_jpg_to_pdf_cta),
    REORDER(R.string.tool_reorder_label, R.string.tool_reorder_subtitle, R.string.tool_reorder_cta),
    DELETE(R.string.tool_delete_label, R.string.tool_delete_subtitle, R.string.tool_delete_cta),
    DOCX_TO_PDF(R.string.tool_docx_to_pdf_label, R.string.tool_docx_to_pdf_subtitle, R.string.tool_docx_to_pdf_cta),
    PDF_TO_DOCX(R.string.tool_pdf_to_docx_label, R.string.tool_pdf_to_docx_subtitle, R.string.tool_pdf_to_docx_cta);

    companion object {
        fun fromKey(key: String) = entries.find { it.name.lowercase() == key.lowercase() } ?: MERGE
    }
}
