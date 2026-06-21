package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.mockRecentFiles
import com.mobiapps.quickpdfhub.navigation.ToolType
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.components.RecentFileRow
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolEntryScreen(
    toolType: ToolType,
    onChooseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onViewAllRecentClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = "QuickPDF",
                onSettingsClick = onSettingsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Dashed upload zone
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = SolidColor(BrandTeal.copy(alpha = 0.5f)),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = null,
                        tint = BrandTeal,
                        modifier = Modifier.size(48.dp),
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Select files",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = toolType.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onChooseClick,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = toolType.ctaLabel,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Recent files section
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                mockRecentFiles.take(3).forEach { file ->
                    RecentFileRow(file = file)
                }
            }
        }
    }
}
