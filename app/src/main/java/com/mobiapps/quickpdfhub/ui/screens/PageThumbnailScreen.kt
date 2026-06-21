package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiapps.quickpdfhub.data.mockPageCount
import com.mobiapps.quickpdfhub.navigation.ToolType
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageThumbnailScreen(
    toolType: ToolType,
    onProcessClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val pages = remember { (1..mockPageCount).map { it }.toMutableStateList() }
    val selected = remember { mutableStateListOf(*Array(mockPageCount) { true }) }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = "QuickPDF",
                onSettingsClick = onSettingsClick,
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onProcessClick,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                ) {
                    Text(
                        text = when (toolType) {
                            ToolType.MERGE -> "Merge ${selected.count { it }} pages"
                            ToolType.SPLIT -> "Split PDF"
                            else -> "Process ${selected.count { it }} pages"
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        ) {
            items(pages.size) { index ->
                PageCell(
                    pageNumber = index + 1,
                    isSelected = selected.getOrElse(index) { false },
                    onToggle = {
                        if (index < selected.size) selected[index] = !selected[index]
                    },
                )
            }
        }
    }
}

@Composable
private fun PageCell(
    pageNumber: Int,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.aspectRatio(0.75f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gray page placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            // Page number badge
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(BrandTeal)
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$pageNumber",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Drag handle
            Icon(
                imageVector = Icons.Outlined.DragIndicator,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(6.dp)
                    .size(16.dp)
                    .align(Alignment.TopEnd),
            )

            // Checkmark
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = BrandTeal,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                        .align(Alignment.BottomEnd),
                )
            }
        }
    }
}
