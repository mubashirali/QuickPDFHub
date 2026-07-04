package com.mobiapps.quickpdfhub.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.data.RecentFile
import com.mobiapps.quickpdfhub.domain.openRecentFile
import com.mobiapps.quickpdfhub.navigation.ToolType
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.components.RecentFileRow
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

private val PDF_MIME = arrayOf("application/pdf")
private val IMAGE_MIME = arrayOf("image/*")
private val DOCX_MIME = arrayOf(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/msword",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolEntryScreen(
    toolType: ToolType,
    onFilesSelected: () -> Unit,
    onSettingsClick: () -> Unit,
    onViewAllRecentClick: () -> Unit,
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Single-file picker (all tools except Merge)
    val singlePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            PdfWorkSession.setInputs(listOf(uri))
            onFilesSelected()
        }
    }

    // Multi-file picker (Merge + JPG_TO_PDF)
    val multiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris: List<Uri> ->
        when {
            uris.isEmpty() -> { /* user cancelled */ }
            toolType == ToolType.MERGE && uris.size < 2 ->
                errorMessage = "Merge requires at least 2 PDF files."
            else -> {
                PdfWorkSession.setInputs(uris)
                onFilesSelected()
            }
        }
    }

    val mimeTypes = when (toolType) {
        ToolType.JPG_TO_PDF -> IMAGE_MIME
        ToolType.DOCX_TO_PDF -> DOCX_MIME
        else -> PDF_MIME
    }

    fun launchPicker() {
        errorMessage = null
        if (toolType == ToolType.MERGE || toolType == ToolType.JPG_TO_PDF)
            multiPicker.launch(mimeTypes)
        else singlePicker.launch(mimeTypes)
    }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = toolType.label,
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

                    if (errorMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = ::launchPicker,
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

            // Recent files
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

            if (PdfWorkSession.recentEntries.isEmpty()) {
                Text(
                    text = "No recent files yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PdfWorkSession.recentEntries.take(3).forEach { file ->
                        RecentFileRow(
                            file = file,
                            onClick = { openRecentFile(context, file.outputUri, file.name, isImage = file.operation == RecentFile.OP_PDF_TO_JPG) },
                        )
                    }
                }
            }
        }
    }
}
