package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

private data class QualityOption(val label: String, val description: String, val quality: Int)

private val qualityOptions = listOf(
    QualityOption("Low compression", "Preserves quality, larger file", 90),
    QualityOption("Medium compression", "Balanced quality and size", 65),
    QualityOption("High compression", "Smallest file, reduced quality", 35),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressOptionsScreen(
    onContinueClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var selectedQuality by remember { mutableIntStateOf(PdfWorkSession.compressionQuality) }

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
                    onClick = {
                        PdfWorkSession.compressionQuality = selectedQuality
                        onContinueClick()
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                ) {
                    Text("Continue", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Compression quality",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Choose how much to compress your PDF. Lower quality produces a smaller file.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            qualityOptions.forEach { option ->
                val isSelected = selectedQuality == option.quality
                Surface(
                    onClick = { selectedQuality = option.quality },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrandTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    tonalElevation = if (isSelected) 0.dp else 1.dp,
                    shadowElevation = if (isSelected) 0.dp else 1.dp,
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, BrandTeal)
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) BrandTeal else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = option.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedQuality = option.quality },
                            colors = RadioButtonDefaults.colors(selectedColor = BrandTeal),
                        )
                    }
                }
            }
        }
    }
}
