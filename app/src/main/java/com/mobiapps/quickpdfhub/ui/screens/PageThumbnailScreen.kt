package com.mobiapps.quickpdfhub.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.navigation.ToolType
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageThumbnailScreen(
    toolType: ToolType,
    onProcessClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    val inputUri = PdfWorkSession.primaryInput

    var totalPages by remember { mutableIntStateOf(0) }
    val thumbnails = remember { mutableStateListOf<Bitmap?>() }

    // DELETE: index → true means keep, false means delete
    val keepPage = remember { mutableStateListOf<Boolean>() }
    // SPLIT: 1-indexed page to split after
    var splitAfterPage by remember { mutableIntStateOf(PdfWorkSession.splitAfterPage.coerceAtLeast(1)) }
    // REORDER: current page order as list of original 0-indexed page indices
    val pageOrder = remember { mutableStateListOf<Int>() }

    // Recycle bitmaps when the composable leaves the composition
    DisposableEffect(inputUri) {
        onDispose { thumbnails.forEach { it?.recycle() } }
    }

    // Load thumbnails from PDF
    LaunchedEffect(inputUri) {
        if (inputUri == null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null
            try {
                val tempFile = File(context.cacheDir, "thumb_input.pdf")
                context.contentResolver.openInputStream(inputUri)?.use { src ->
                    FileOutputStream(tempFile).use { dst -> src.copyTo(dst) }
                }
                pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(pfd)
                val count = renderer.pageCount

                withContext(Dispatchers.Main) {
                    totalPages = count
                    repeat(count) { thumbnails.add(null) }
                    repeat(count) { keepPage.add(true) }
                    pageOrder.addAll(0 until count)
                    if (splitAfterPage > count - 1) splitAfterPage = maxOf(1, count - 1)
                }

                for (i in 0 until count) {
                    val page = renderer.openPage(i)
                    val scale = 280f / page.width.coerceAtLeast(1)
                    val bw = 280
                    val bh = (page.height * scale).toInt().coerceAtLeast(1)
                    val bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    withContext(Dispatchers.Main) { thumbnails[i] = bmp }
                }
            } catch (_: Exception) { /* placeholders remain */ } finally {
                renderer?.close() ?: pfd?.close()
            }
        }
    }

    val buttonLabel = when (toolType) {
        ToolType.SPLIT -> "Split after page $splitAfterPage"
        ToolType.DELETE -> "Delete ${keepPage.count { !it }} page${if (keepPage.count { !it } == 1) "" else "s"}"
        ToolType.REORDER -> "Apply new order"
        else -> "Process"
    }

    Scaffold(
        topBar = {
            QuickPdfTopBar(title = toolType.label, onSettingsClick = onSettingsClick)
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        when (toolType) {
                            ToolType.SPLIT -> PdfWorkSession.splitAfterPage = splitAfterPage
                            ToolType.DELETE -> PdfWorkSession.pagesToDelete =
                                keepPage.indices.filter { !keepPage[it] }
                            ToolType.REORDER -> PdfWorkSession.reorderedPageIndices = pageOrder.toList()
                            else -> {}
                        }
                        onProcessClick()
                    },
                    enabled = totalPages > 0,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                ) {
                    Text(text = buttonLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (toolType == ToolType.REORDER) {
            ReorderPagesList(
                pageOrder = pageOrder,
                thumbnails = thumbnails,
                innerPadding = innerPadding,
            )
        } else {
            PageGrid(
                toolType = toolType,
                thumbnails = thumbnails,
                keepPage = keepPage,
                splitAfterPage = splitAfterPage,
                onToggleDelete = { idx -> if (idx < keepPage.size) keepPage[idx] = !keepPage[idx] },
                onSetSplit = { page -> splitAfterPage = page },
                innerPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun PageGrid(
    toolType: ToolType,
    thumbnails: List<Bitmap?>,
    keepPage: List<Boolean>,
    splitAfterPage: Int,
    onToggleDelete: (Int) -> Unit,
    onSetSplit: (Int) -> Unit,
    innerPadding: PaddingValues,
) {
    val hint = if (toolType == ToolType.SPLIT) "Tap a page to split after it"
               else "Tap pages to mark for deletion"
    val isSplit = toolType == ToolType.SPLIT

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        if (isSplit) {
            // Part 1 pages (1..splitAfterPage)
            val part1Count = splitAfterPage.coerceAtMost(thumbnails.size)
            items(part1Count) { index ->
                val pageNum = index + 1
                PageCell(
                    pageNumber = pageNum,
                    thumbnail = thumbnails.getOrNull(index),
                    isMarkedForDelete = false,
                    isSplitPoint = splitAfterPage == pageNum,
                    showSplitIndicator = true,
                    onClick = { if (index < thumbnails.size - 1) onSetSplit(pageNum) },
                )
            }

            // Full-width split divider (only when there are pages after the split)
            if (splitAfterPage < thumbnails.size) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    SplitDivider(splitAfterPage = splitAfterPage)
                }
            }

            // Part 2 pages (splitAfterPage+1..end)
            val part2Count = (thumbnails.size - splitAfterPage).coerceAtLeast(0)
            items(part2Count) { i ->
                val index = splitAfterPage + i
                val pageNum = index + 1
                PageCell(
                    pageNumber = pageNum,
                    thumbnail = thumbnails.getOrNull(index),
                    isMarkedForDelete = false,
                    isSplitPoint = false,
                    showSplitIndicator = false,
                    onClick = { onSetSplit(pageNum - 1) }, // tapping part-2 page moves split before it
                )
            }
        } else {
            items(thumbnails.size) { index ->
                val pageNum = index + 1
                PageCell(
                    pageNumber = pageNum,
                    thumbnail = thumbnails.getOrNull(index),
                    isMarkedForDelete = !keepPage.getOrElse(index) { true },
                    isSplitPoint = false,
                    showSplitIndicator = false,
                    onClick = { onToggleDelete(index) },
                )
            }
        }
    }
}

@Composable
private fun SplitDivider(splitAfterPage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BrandTeal, thickness = 1.5.dp)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandTeal,
        ) {
            Text(
                text = "✂  Split after page $splitAfterPage",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = BrandTeal, thickness = 1.5.dp)
    }
}

@Composable
private fun PageCell(
    pageNumber: Int,
    thumbnail: Bitmap?,
    isMarkedForDelete: Boolean,
    isSplitPoint: Boolean,
    showSplitIndicator: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = when {
        isMarkedForDelete -> MaterialTheme.colorScheme.error
        isSplitPoint -> BrandTeal
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = if (isMarkedForDelete || isSplitPoint) {
            androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        } else null,
        modifier = Modifier.aspectRatio(0.75f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = "Page $pageNumber",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }

            // Page number badge
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isMarkedForDelete) MaterialTheme.colorScheme.error else BrandTeal)
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$pageNumber",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Split point pill at bottom of selected card
            if (showSplitIndicator && isSplitPoint) {
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = BrandTeal,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = "Split here",
                        fontSize = 11.sp,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderPagesList(
    pageOrder: androidx.compose.runtime.snapshots.SnapshotStateList<Int>,
    thumbnails: List<Bitmap?>,
    innerPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
    ) {
        item {
            Text(
                text = "Use arrows to reorder pages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        items(pageOrder.size) { pos ->
            val originalIndex = pageOrder[pos]
            val thumbnail = thumbnails.getOrNull(originalIndex)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        if (thumbnail != null) {
                            Image(
                                bitmap = thumbnail.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Page ${originalIndex + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Position ${pos + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Column {
                        IconButton(
                            onClick = {
                                if (pos > 0) {
                                    val tmp = pageOrder[pos - 1]
                                    pageOrder[pos - 1] = pageOrder[pos]
                                    pageOrder[pos] = tmp
                                }
                            },
                            enabled = pos > 0,
                        ) {
                            Icon(
                                Icons.Outlined.ArrowUpward,
                                contentDescription = "Move up",
                                tint = if (pos > 0) BrandTeal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            )
                        }
                        IconButton(
                            onClick = {
                                if (pos < pageOrder.size - 1) {
                                    val tmp = pageOrder[pos + 1]
                                    pageOrder[pos + 1] = pageOrder[pos]
                                    pageOrder[pos] = tmp
                                }
                            },
                            enabled = pos < pageOrder.size - 1,
                        ) {
                            Icon(
                                Icons.Outlined.ArrowDownward,
                                contentDescription = "Move down",
                                tint = if (pos < pageOrder.size - 1) BrandTeal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }
        }
    }
}
