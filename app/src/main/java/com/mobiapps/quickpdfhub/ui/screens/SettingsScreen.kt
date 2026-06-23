package com.mobiapps.quickpdfhub.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.ThemeManager
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(context = context, onDismiss = { showAboutDialog = false })
    }
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear cache") },
            text = { Text("This will delete all temporary files created by QuickPDF. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearCacheDialog = false
                    clearAppCache(context)
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = "Settings",
                onBackClick = onBack,
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
            item {
                SwitchSettingsRow(
                    icon = Icons.Outlined.DarkMode,
                    label = "Dark mode",
                    checked = ThemeManager.isDarkMode,
                    onCheckedChange = { ThemeManager.setDarkMode(it) },
                )
                RowDivider()
            }

            item {
                ActionSettingsRow(
                    icon = Icons.Outlined.Delete,
                    label = "Clear cache",
                    onClick = { showClearCacheDialog = true },
                )
                RowDivider()
            }

            item {
                ActionSettingsRow(
                    icon = Icons.Outlined.Info,
                    label = "About",
                    onClick = { showAboutDialog = true },
                )
                RowDivider()
            }

            item {
                ActionSettingsRow(
                    icon = Icons.Outlined.Lock,
                    label = "Privacy Policy",
                    onClick = {
                        openUrl(
                            context,
                            "https://docs.google.com/document/d/10-CJMcAz9kCtdFFia1Po4TAk40YdTrdZMh3cgjHuevk/edit?tab=t.0",
                        )
                    },
                )
                RowDivider()
            }

            item {
                ActionSettingsRow(
                    icon = Icons.Outlined.Star,
                    label = "Rate this app",
                    onClick = { rateApp(context) },
                )
            }
        }
    }
}

@Composable
private fun SwitchSettingsRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ActionSettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.background,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 56.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    )
}

@Composable
private fun AboutDialog(context: Context, onDismiss: () -> Unit) {
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        }.getOrDefault("1.0")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        title = { Text("QuickPDF Hub", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Version $versionName")
                Spacer(Modifier.height(4.dp))
                Text(
                    "All PDF operations run entirely on your device. No files are uploaded to any server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
    )
}

private fun clearAppCache(context: Context) {
    try {
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Could not clear cache", Toast.LENGTH_SHORT).show()
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No browser found", Toast.LENGTH_SHORT).show()
    }
}

private fun rateApp(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (e: ActivityNotFoundException) {
        // Fallback to browser if Play Store not installed
        openUrl(context, "https://play.google.com/store/apps/details?id=$packageName")
    }
}
