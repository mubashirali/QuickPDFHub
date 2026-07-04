package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.R
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

private data class FeatureRow(
    val featureRes: Int,
    val freeRes: Int?,   // null = dash
    val pro: Boolean,
)

private val features = listOf(
    FeatureRow(R.string.upgrade_feature_ads, R.string.upgrade_feature_ads_free, true),
    FeatureRow(R.string.upgrade_feature_batch, null, true),
    FeatureRow(R.string.upgrade_feature_size_cap, null, true),
    FeatureRow(R.string.upgrade_feature_priority, null, true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(
    onSubscribeClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = stringResource(R.string.app_name),
                onSettingsClick = onSettingsClick,
                showSearch = false,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(48.dp),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.upgrade_title),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.upgrade_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            // Feature comparison table
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(
                            stringResource(R.string.upgrade_feature_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.5f),
                        )
                        Text(
                            stringResource(R.string.upgrade_feature_free),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            stringResource(R.string.upgrade_feature_pro),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandTeal,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    features.forEachIndexed { index, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(row.featureRes),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1.5f),
                            )
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (row.freeRes != null) {
                                    Text(
                                        text = stringResource(row.freeRes),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Text(
                                        text = "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (row.pro) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = stringResource(R.string.upgrade_included),
                                        tint = BrandTeal,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                        if (index < features.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onSubscribeClick,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    text = stringResource(R.string.upgrade_subscribe),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
