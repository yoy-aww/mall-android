package com.example.mall_android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = Color(0xFF7F0000),
    secondary = BrandSecondary,
    onSecondary = Color.White,
    background = BrandBackground,
    onBackground = BrandText,
    surface = BrandSurface,
    onSurface = BrandText,
    surfaceVariant = Color(0xFFFAFAFA),
    onSurfaceVariant = BrandTextSecondary,
    outline = BrandBorder,
    error = Color(0xFFEF5350)
)

@Composable
fun MallTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
