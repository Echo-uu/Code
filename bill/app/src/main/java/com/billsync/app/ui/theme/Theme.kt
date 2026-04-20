package com.billsync.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = PrimaryLight,
    secondary = Secondary,
    onSecondary = Surface,
    secondaryContainer = Secondary.copy(alpha = 0.1f),
    background = Background,
    onBackground = DarkBackground,
    surface = Surface,
    onSurface = DarkBackground,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = DarkSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = PrimaryDark,
    secondary = SecondaryDark,
    onSecondary = Surface,
    background = DarkBackground,
    onBackground = Surface,
    surface = DarkSurface,
    onSurface = Surface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Surface.copy(alpha = 0.7f),
)

@Composable
fun BillSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
