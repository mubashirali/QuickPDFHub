package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.data.RecentFile
import com.mobiapps.quickpdfhub.domain.openRecentFile
import com.mobiapps.quickpdfhub.navigation.ToolType
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.components.RecentFileCard
import com.mobiapps.quickpdfhub.ui.theme.BannerBgLight
import com.mobiapps.quickpdfhub.ui.theme.BannerTextLight
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

private data class ToolCard(
    val tool: ToolType,
    val icon: ImageVector,
)

private val primaryTools = listOf(
    ToolCard(ToolType.MERGE, Icons.Outlined.MergeType),
    ToolCard(ToolType.SPLIT, Icons.Outlined.CallSplit),
    ToolCard(ToolType.COMPRESS, Icons.Outlined.Compress),
    ToolCard(ToolType.PDF_TO_JPG, Icons.Outlined.Image),
    ToolCard(ToolType.JPG_TO_PDF, Icons.Outlined.PictureAsPdf),
)

private val moreTools = listOf(
    ToolCard(ToolType.REORDER, Icons.Outlined.SwapVert),
    ToolCard(ToolType.DELETE, Icons.Outlined.DeleteOutline),
    ToolCard(ToolType.DOCX_TO_PDF, Icons.Outlined.Description),
    ToolCard(ToolType.PDF_TO_DOCX, Icons.Outlined.TextSnippet),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onToolClick: (ToolType) -> Unit,
    onViewAllRecentClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var moreToolsExpanded by remember { mutableStateOf(true) }
    val recentFiles = PdfWorkSession.recentEntries.take(4)

    // loadFromRoom is guarded by roomLoaded — safe to call from multiple screens; only the first call loads data.
    LaunchedEffect(Unit) {
        PdfWorkSession.loadFromRoom(context)
    }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = "QuickPDF Hub",
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // "Processed on your device" banner
            ProcessedOnDeviceBanner()

            Spacer(Modifier.height(16.dp))

            // Primary tool grid (2 columns)
            val rows = primaryTools.chunked(2)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rows.forEach { rowTools ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowTools.forEach { toolCard ->
                            HomeToolCard(
                                toolCard = toolCard,
                                modifier = Modifier.weight(1f),
                                onClick = { onToolClick(toolCard.tool) },
                            )
                        }
                        // Fill empty slot in last row if odd number
                        if (rowTools.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // More tools section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { moreToolsExpanded = !moreToolsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "More tools",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Icon(
                        imageVector = if (moreToolsExpanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                AnimatedVisibility(visible = moreToolsExpanded) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            moreTools.forEach { toolCard ->
                                MoreToolChip(
                                    label = toolCard.tool.label.replace(" ", "\n"),
                                    onClick = { onToolClick(toolCard.tool) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Recent files section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent files",
                    style = MaterialTheme.typography.headlineSmall,
                )
                TextButton(onClick = onViewAllRecentClick) {
                    Text("View all", color = BrandTeal)
                }
            }

            Spacer(Modifier.height(8.dp))

            if (recentFiles.isEmpty()) {
                EmptyRecentFiles()
            } else {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    recentFiles.forEach { file ->
                        RecentFileCard(
                            file = file,
                            onClick = { openRecentFile(context, file.outputUri, file.name, isImage = file.operation == RecentFile.OP_PDF_TO_JPG) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProcessedOnDeviceBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BannerBgLight)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Security,
            contentDescription = null,
            tint = BannerTextLight,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Processed on your device",
            style = MaterialTheme.typography.labelSmall,
            color = BannerTextLight,
        )
    }
}

@Composable
private fun HomeToolCard(
    toolCard: ToolCard,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = toolCard.icon,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = toolCard.tool.label,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = toolCard.tool.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MoreToolChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun EmptyRecentFiles() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No recent files yet",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Pick a tool above to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
