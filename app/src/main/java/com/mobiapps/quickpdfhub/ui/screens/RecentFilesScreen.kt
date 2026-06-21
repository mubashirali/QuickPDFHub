package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.RecentFile
import com.mobiapps.quickpdfhub.data.mockRecentFiles
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.components.RecentFileRow
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

private val filterChips = listOf("All", "Merge", "Compress", "Convert")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentFilesScreen(
    onSettingsClick: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val grouped: Map<String, List<RecentFile>> = mockRecentFiles
        .let { files ->
            if (selectedFilter == "All") files
            else files.filter { it.operation.contains(selectedFilter, ignoreCase = true) }
        }
        .groupBy { it.date }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = "QuickPDF Hub",
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

            grouped.forEach { (date, files) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                item {
                    Text(
                        text = "Recent files",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(files) { file ->
                    RecentFileRow(
                        file = file,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }

            if (grouped.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        Text(
                            text = "No files found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
