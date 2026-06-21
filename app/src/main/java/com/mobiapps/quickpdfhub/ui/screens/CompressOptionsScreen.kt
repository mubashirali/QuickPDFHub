package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal
import com.mobiapps.quickpdfhub.ui.theme.TealContainer

private enum class CompressionLevel(
    val label: String,
    val subtitle: String,
    val estimate: String,
) {
    LOW("Low compression", "Best quality", "est. 2.1 MB"),
    MEDIUM("Medium compression", "Recommended", "est. 1.2 MB"),
    HIGH("High compression", "Smallest file", "est. 680 KB"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressOptionsScreen(
    onCompressClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var selected by remember { mutableStateOf(CompressionLevel.MEDIUM) }
    var advancedSlider by remember { mutableFloatStateOf(0.6f) }

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
                    onClick = onCompressClick,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                ) {
                    Text("Compress PDF", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CompressionLevel.entries.forEach { level ->
                CompressionOptionRow(
                    level = level,
                    isSelected = selected == level,
                    onClick = { selected = level },
                )
            }

            Spacer(Modifier.height(4.dp))

            // Advanced slider row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Advanced", style = MaterialTheme.typography.titleSmall)
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = advancedSlider,
                        onValueChange = { advancedSlider = it },
                        colors = SliderDefaults.colors(
                            thumbColor = BrandTeal,
                            activeTrackColor = BrandTeal,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompressionOptionRow(
    level: CompressionLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) BrandTeal else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) TealContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = level.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = level.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = level.estimate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
