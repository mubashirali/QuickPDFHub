package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobiapps.quickpdfhub.R
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal
import com.mobiapps.quickpdfhub.ui.theme.ErrorContainer
import com.mobiapps.quickpdfhub.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(
    title: String? = null,
    body: String? = null,
    onTryAgain: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val displayTitle = title ?: stringResource(R.string.error_title_default)
    val displayBody = body ?: stringResource(R.string.error_body_default)
    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = stringResource(R.string.app_name),
                onSettingsClick = onSettingsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ErrorContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(44.dp),
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = displayBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onTryAgain,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(52.dp),
                ) {
                    Text(stringResource(R.string.error_try_again), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
