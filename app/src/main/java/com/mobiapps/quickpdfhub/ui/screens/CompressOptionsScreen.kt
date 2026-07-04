package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.R
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

private data class QualityOption(val labelRes: Int, val descriptionRes: Int, val quality: Int)

private val qualityOptions = listOf(
    QualityOption(R.string.compress_options_low, R.string.compress_options_low_description, 90),
    QualityOption(R.string.compress_options_medium, R.string.compress_options_medium_description, 65),
    QualityOption(R.string.compress_options_high, R.string.compress_options_high_description, 35),
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
                title = stringResource(R.string.compress_options_title),
                onSettingsClick = onSettingsClick,
                showSearch = false,
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
                    Text(stringResource(R.string.compress_options_continue), fontWeight = FontWeight.SemiBold)
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
                text = stringResource(R.string.compress_options_label),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(R.string.compress_options_description),
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
                                text = stringResource(option.labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) BrandTeal else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(option.descriptionRes),
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
