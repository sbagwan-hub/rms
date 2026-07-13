package com.tionix.rms.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = BluePrimary.copy(alpha = 0.1f),
    onPrimaryContainer = BluePrimary,
    secondary = InfoBlue,
    onSecondary = SurfaceWhite,
    secondaryContainer = BorderSlate200,
    onSecondaryContainer = TextSlate900,
    tertiary = InfoBlue,
    onTertiary = SurfaceWhite,
    background = BackgroundSlate,
    onBackground = TextSlate900,
    surface = SurfaceWhite,
    onSurface = TextSlate900,
    surfaceVariant = BackgroundSlate,
    onSurfaceVariant = TextSlate600,
    outline = BorderSlate200,
    error = ErrorRose,
    onError = SurfaceWhite
)

@Composable
fun RMSTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
