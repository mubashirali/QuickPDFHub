package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.data.RecentFile
import com.mobiapps.quickpdfhub.domain.openRecentFile
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.components.RecentFileRow
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

private val filterChips = listOf("All", "Compress", "Merge", "Split", "Delete", "Reorder", RecentFile.OP_PDF_TO_JPG, RecentFile.OP_JPG_TO_PDF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentFilesScreen(
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }

    // loadFromRoom is guarded by roomLoaded — safe to call from multiple screens; only the first call loads data.
    LaunchedEffect(Unit) {
        PdfWorkSession.loadFromRoom(context)
    }

    val grouped = remember(PdfWorkSession.recentEntries.toList(), selectedFilter) {
        PdfWorkSession.recentEntries
            .let { files ->
                if (selectedFilter == "All") files
                else files.filter { it.operation.contains(selectedFilter, ignoreCase = true) }
            }
            .groupBy { it.date }
    }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = "Recent Files",
                onSettingsClick = onSettingsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Filter chips
            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filterChips) { chip ->
                        val isActive = chip == selectedFilter
                        if (isActive) {
                            Button(
                                onClick = { selectedFilter = chip },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.height(36.dp),
                            ) {
                                Text(chip, style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { selectedFilter = chip },
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.height(36.dp),
                            ) {
                                Text(chip, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            if (grouped.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No files found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                grouped.forEach { (date, files) ->
                    item(key = "header_$date") {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = 16.dp, end = 16.dp,
                                top = 12.dp, bottom = 4.dp,
                            ),
                        )
                    }
                    itemsIndexed(files, key = { index, file -> "${file.date}_${index}_${file.outputUri}_${file.name}" }) { _, file ->
                        RecentFileRow(
                            file = file,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            onClick = { openRecentFile(context, file.outputUri, file.name, isImage = file.operation == RecentFile.OP_PDF_TO_JPG) },
                        )
                    }
                    item(key = "spacer_$date") { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
