package com.mobiapps.quickpdfhub.navigation

object Route {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val TOOL_ENTRY = "tool_entry/{toolType}"
    const val PAGE_THUMBNAIL = "page_thumbnail/{toolType}"
    const val COMPRESS_OPTIONS = "compress_options"
    const val PROCESSING = "processing/{toolType}"
    const val RESULT = "result/{toolType}"
    const val RECENT_FILES = "recent_files"
    const val SETTINGS = "settings"
    const val UPGRADE = "upgrade"
    const val PERMISSION_RATIONALE = "permission_rationale"
    const val ERROR = "error"

    fun toolEntry(toolType: String) = "tool_entry/$toolType"
    fun pageThumbnail(toolType: String) = "page_thumbnail/$toolType"
    fun processing(toolType: String) = "processing/$toolType"
    fun result(toolType: String) = "result/$toolType"
}

enum class ToolType(val label: String, val subtitle: String, val ctaLabel: String) {
    MERGE("Merge PDF", "Merge supports multiple PDFs", "Choose PDFs"),
    SPLIT("Split PDF", "Split supports a single PDF", "Choose PDF"),
    COMPRESS("Compress PDF", "Reduce file size while keeping quality", "Choose PDF"),
    PDF_TO_JPG("PDF → JPG", "Export each page as a crisp image", "Choose PDF"),
    JPG_TO_PDF("JPG → PDF", "Combine images into a clean PDF", "Choose Images"),
    REORDER("Reorder Pages", "Drag pages to reorder", "Choose PDF"),
    DELETE("Delete Pages", "Remove unwanted pages", "Choose PDF");

    companion object {
        fun fromKey(key: String) = entries.find { it.name.lowercase() == key.lowercase() } ?: MERGE
    }
}
