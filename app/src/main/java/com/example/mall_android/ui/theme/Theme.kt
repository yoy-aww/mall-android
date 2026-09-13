package com.example.mall_android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFe9f0e8),
    onPrimaryContainer = Color(0xFF1a3020),
    secondary = BrandAccent,
    onSecondary = Color.White,
    background = BrandBackground,
    onBackground = BrandText,
    surface = BrandSurface,
    onSurface = BrandText,
    surfaceVariant = Color(0xFFf5f3ee),
    onSurfaceVariant = BrandTextSecondary,
    outline = BrandBorder,
    error = Color(0xFFc0392b)
)

@Composable
fun MallTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
