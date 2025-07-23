package com.kairaxus.bundymom.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63),  // Pink 500
    secondary = Color(0xFFF8BBD0),  // Pink 200
    tertiary = Color(0xFFFCE4EC),   // Pink 50
    background = Color(0xFFFFF5F7), // Very light pink
    surface = Color(0xFFFFF5F7),    // Very light pink
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    // You can add more color customizations here
)

@Composable
fun BundyMomTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Using only light theme for now

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}