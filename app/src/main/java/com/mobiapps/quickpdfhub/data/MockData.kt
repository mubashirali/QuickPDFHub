package com.mobiapps.quickpdfhub.data

data class RecentFile(
    val name: String,
    val operation: String,
    val size: String,
    val date: String,
)

val mockRecentFiles = listOf(
    RecentFile("Lease_packet.pdf", "Merged", "2.8 MB", "Today"),
    RecentFile("Resume_final.pdf", "Compressed", "812 KB", "Today"),
    RecentFile("Invoice_042.pdf", "PDF → JPG", "1.4 MB", "Yesterday"),
    RecentFile("Contract_v3.pdf", "Split", "540 KB", "Yesterday"),
    RecentFile("Report_Q2.pdf", "Merged", "3.1 MB", "Jun 19"),
    RecentFile("Photos_scan.pdf", "JPG → PDF", "2.2 MB", "Jun 19"),
)

val mockPageCount = 9
