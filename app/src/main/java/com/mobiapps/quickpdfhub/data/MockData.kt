package com.mobiapps.quickpdfhub.data

data class RecentFile(
    val name: String,
    val operation: String,
    val size: String,
    val date: String,
    val outputUri: String? = null,
) {
    companion object {
        const val OP_PDF_TO_JPG = "PDF → JPG"
        const val OP_JPG_TO_PDF = "JPG → PDF"
    }
}
