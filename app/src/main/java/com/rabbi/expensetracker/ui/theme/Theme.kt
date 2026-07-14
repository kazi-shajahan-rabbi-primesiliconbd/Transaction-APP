package com.rabbi.expensetracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = TakaGreen,
    onPrimary = SurfaceLight,
    secondary = TakaGold,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = TakaGreenLight
)

private val DarkColors = darkColorScheme(
    primary = TakaGold,
    onPrimary = SurfaceDark,
    secondary = TakaGreen,
    background = SurfaceDark,
    surface = Color0F(),
)

// small helper to avoid importing Color again at top-level ambiguity
private fun Color0F() = androidx.compose.ui.graphics.Color(0xFF191D19)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
