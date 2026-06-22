package com.mobiapps.quickpdfhub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mobiapps.quickpdfhub.data.ThemeManager

private val LightColorScheme = lightColorScheme(
    primary = BrandTeal,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandTealLight,
    onPrimary = Color.Black,
    primaryContainer = OnTealContainer,
    onPrimaryContainer = TealContainer,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorRed,
    errorContainer = Color(0xFF3B1010),
    onErrorContainer = Color(0xFFFFB4AB),
)

@Composable
fun QuickPDFHubTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = if (ThemeManager.isDarkMode) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
