package com.omondit.alarmfocus.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * ADHD-Optimized Theme
 *
 * Provides calming, high-contrast, accessibility-first theming
 * with smooth animations and intuitive visual hierarchy
 */

// Local composition for accessing custom theme properties
val LocalADHDColors = staticCompositionLocalOf { ADHDColors }

@Composable
fun AlarmFocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default for consistent ADHD-optimized colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ADHDDarkColorScheme
        else -> ADHDLightColorScheme
    }

    CompositionLocalProvider(
        LocalADHDColors provides ADHDColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ADHDTypography,
            shapes = ADHDShapes,
            content = content
        )
    }
}
