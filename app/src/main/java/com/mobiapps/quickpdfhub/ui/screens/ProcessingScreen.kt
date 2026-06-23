package com.mobiapps.quickpdfhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiapps.quickpdfhub.navigation.ToolType
import com.mobiapps.quickpdfhub.ui.components.QuickPdfTopBar
import com.mobiapps.quickpdfhub.ui.theme.BrandTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    toolType: String,
    onCancel: () -> Unit,
    onSettingsClick: () -> Unit,
    onFinished: () -> Unit,
    onError: (String) -> Unit,
) {
    val viewModel = viewModel<ProcessingViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(toolType) {
        viewModel.start(toolType)
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is ProcessingViewModel.State.Done -> onFinished()
            is ProcessingViewModel.State.Failed -> onError(s.message)
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            QuickPdfTopBar(
                title = ToolType.fromKey(toolType).label,
                onSettingsClick = onSettingsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(96.dp),
                        color = BrandTeal,
                        strokeWidth = 4.dp,
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = BrandTeal,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Processing your PDF…",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "This stays on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.height(44.dp),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
