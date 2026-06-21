package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar

private data class SettingsRow(val icon: ImageVector, val label: String)

private val settingsItems = listOf(
    SettingsRow(Icons.Outlined.DarkMode, "Dark mode"),
    SettingsRow(Icons.Outlined.Download, "Default save location"),
    SettingsRow(Icons.Outlined.Delete, "Clear cache"),
    SettingsRow(Icons.Outlined.Info, "About"),
    SettingsRow(Icons.Outlined.Lock, "Privacy Policy"),
    SettingsRow(Icons.Outlined.Notifications, "Rate this app"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSettingsClick: () -> Unit = {},
) {
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
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(settingsItems.size) { index ->
                val item = settingsItems[index]
                SettingsRowItem(
                    icon = item.icon,
                    label = item.label,
                    onClick = {},
                )
                if (index < settingsItems.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 56.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
