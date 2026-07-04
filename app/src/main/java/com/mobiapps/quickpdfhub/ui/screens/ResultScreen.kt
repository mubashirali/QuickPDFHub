package com.mobiapps.quickpdfhub.ui.screens

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.R
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.domain.formatBytes
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal
import com.mobiapps.quickpdfhub.ui.theme.TealContainer
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onGoHome: () -> Unit,
    onDoAnotherClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    val result = PdfWorkSession.lastResult
    val isImageResult = (result?.pageCount ?: 0) > 0
    val isSplitResult = !isImageResult && (result?.outputUris?.size ?: 0) > 1

    val title = when {
        isImageResult -> stringResource(R.string.result_title_images_ready)
        isSplitResult -> stringResource(R.string.result_title_pdf_split)
        else -> stringResource(R.string.result_title_pdf_ready)
    }
    val subtitle = when {
        isImageResult -> "${result?.pageCount} page${if ((result?.pageCount ?: 0) == 1) "" else "s"} exported as JPEG"
        isSplitResult -> "Split into ${result?.outputUris?.size ?: 2} parts"
        else -> result?.outputFileName ?: "output.pdf"
    }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = stringResource(R.string.app_name),
                onBackClick = onGoHome,
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
            // Success checkmark
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(TealContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = BrandTeal,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            // Result card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isImageResult) Icons.Outlined.Image else Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (isImageResult) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${result?.pageCount}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTeal,
                                )
                                Text(
                                    text = stringResource(R.string.result_label_pages),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Text(
                                text = "·",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = result?.sizeAfterBytes?.formatBytes() ?: "—",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stringResource(R.string.result_label_total_size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        val beforeSize = result?.sizeBeforeBytes?.formatBytes() ?: "—"
                        val afterSize = result?.sizeAfterBytes?.formatBytes() ?: "—"
                        val sizeReduced = (result?.sizeAfterBytes ?: 0L) < (result?.sizeBeforeBytes ?: 0L)
                        val afterColor = if (sizeReduced) BrandTeal else MaterialTheme.colorScheme.onSurface

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = beforeSize,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stringResource(R.string.result_label_before),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Text(
                                text = "→",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = afterSize,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = afterColor,
                                )
                                Text(
                                    text = stringResource(R.string.result_label_after),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Share
                Button(
                    onClick = {
                        val res = result ?: return@Button
                        val uris = res.outputUris.takeIf { it.isNotEmpty() } ?: listOf(res.outputUri)
                        val intent = if (uris.size > 1) {
                            val mimeType = if (isImageResult) "image/jpeg" else "application/pdf"
                            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = mimeType
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        } else {
                            Intent(Intent.ACTION_SEND).apply {
                                type = if (isImageResult) "image/jpeg" else "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uris.first())
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Outlined.IosShare, contentDescription = "Share")
                }

                // Save
                OutlinedButton(
                    onClick = {
                        val res = result ?: return@OutlinedButton
                        val urisToSave = res.outputUris.takeIf { it.isNotEmpty() }
                            ?: listOf(res.outputUri)
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                urisToSave.forEachIndexed { idx, saveUri ->
                                    val fileName = when {
                                        isImageResult -> {
                                            val base = res.outputFileName.removeSuffix(".jpg")
                                                .substringBeforeLast("_page")
                                            "${base}_page${idx + 1}.jpg"
                                        }
                                        isSplitResult -> {
                                            val base = res.outputFileName.removeSuffix(".pdf")
                                                .removeSuffix("_part1")
                                            "${base}_part${idx + 1}.pdf"
                                        }
                                        else -> res.outputFileName
                                    }
                                    val mimeType = if (isImageResult) "image/jpeg" else "application/pdf"
                                    val collection = if (isImageResult) {
                                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                                    } else {
                                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                                    }
                                    val values = ContentValues().apply {
                                        put(if (isImageResult) MediaStore.Images.Media.DISPLAY_NAME else MediaStore.Downloads.DISPLAY_NAME, fileName)
                                        put(if (isImageResult) MediaStore.Images.Media.MIME_TYPE else MediaStore.Downloads.MIME_TYPE, mimeType)
                                        put(if (isImageResult) MediaStore.Images.Media.IS_PENDING else MediaStore.Downloads.IS_PENDING, 1)
                                    }
                                    val itemUri = context.contentResolver.insert(collection, values)!!
                                    context.contentResolver.openOutputStream(itemUri)?.use { out ->
                                        context.contentResolver.openInputStream(saveUri)?.use { it.copyTo(out) }
                                    }
                                    val pendingKey = if (isImageResult) MediaStore.Images.Media.IS_PENDING else MediaStore.Downloads.IS_PENDING
                                    context.contentResolver.update(itemUri, ContentValues().apply { put(pendingKey, 0) }, null, null)
                                }
                            } else {
                                val dir = if (isImageResult) {
                                    @Suppress("DEPRECATION")
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                } else {
                                    @Suppress("DEPRECATION")
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                }
                                urisToSave.forEachIndexed { idx, saveUri ->
                                    val fileName = when {
                                        isImageResult && urisToSave.size > 1 -> {
                                            val base = res.outputFileName.removeSuffix(".jpg")
                                                .substringBeforeLast("_page")
                                            "${base}_page${idx + 1}.jpg"
                                        }
                                        isSplitResult -> {
                                            val base = res.outputFileName.removeSuffix(".pdf")
                                                .removeSuffix("_part1")
                                            "${base}_part${idx + 1}.pdf"
                                        }
                                        else -> res.outputFileName
                                    }
                                    val destFile = File(dir, fileName)
                                    context.contentResolver.openInputStream(saveUri)?.use { src ->
                                        FileOutputStream(destFile).use { src.copyTo(it) }
                                    }
                                }
                            }
                            val msg = if (isImageResult && urisToSave.size > 1)
                                context.getString(R.string.result_save_multiple_images, urisToSave.size)
                            else if (isImageResult) context.getString(R.string.result_save_single_image)
                            else context.getString(R.string.result_save_pdf)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.result_save_failed, e.message), Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Outlined.SaveAlt, contentDescription = stringResource(R.string.result_icon_save))
                }

                // Open
                OutlinedButton(
                    onClick = {
                        val openUri = result?.outputUri ?: return@OutlinedButton
                        val mimeType = if (isImageResult) "image/jpeg" else "application/pdf"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(openUri, mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.result_no_app_found),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                ) {
                    Text(stringResource(R.string.result_button_open))
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onDoAnotherClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
            ) {
                Text(
                    text = stringResource(R.string.result_button_do_another),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
