package com.mobiapps.quickpdfhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.data.RecentFile
import com.mobiapps.quickpdfhub.ui.theme.ImageIconBlue
import com.mobiapps.quickpdfhub.ui.theme.ImageIconBlueBg
import com.mobiapps.quickpdfhub.ui.theme.PdfIconRed
import com.mobiapps.quickpdfhub.ui.theme.PdfIconRedBg

private fun RecentFile.isImageOutput(): Boolean =
    operation == RecentFile.OP_PDF_TO_JPG ||
    name.endsWith(".jpg", ignoreCase = true) ||
    name.endsWith(".jpeg", ignoreCase = true)

@Composable
fun RecentFileRow(
    file: RecentFile,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isImage = file.isImageOutput()
    val iconBg = if (isImage) ImageIconBlueBg else PdfIconRedBg
    val iconTint = if (isImage) ImageIconBlue else PdfIconRed
    val icon = if (isImage) Icons.Filled.Image else Icons.Filled.Description

    Surface(
        modifier = modifier.fillMaxWidth().semantics { role = Role.Button },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isImage) "Image file" else "PDF file",
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${file.operation} · ${file.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = file.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Compact card variant used in horizontal scroll on Home
@Composable
fun RecentFileCard(
    file: RecentFile,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isImage = file.isImageOutput()
    val iconBg = if (isImage) ImageIconBlueBg else PdfIconRedBg
    val iconTint = if (isImage) ImageIconBlue else PdfIconRed
    val icon = if (isImage) Icons.Filled.Image else Icons.Filled.Description

    Surface(
        modifier = modifier.width(140.dp).semantics { role = Role.Button },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isImage) "Image file" else "PDF file",
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = file.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${file.operation} · ${file.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = file.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
