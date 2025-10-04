package com.omondit.alarmfocus.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * ADHD-Optimized Color Palette
 *
 * Design Rationale:
 * - Cool, calming blues and purples reduce anxiety and promote focus
 * - High contrast ratios (≥4.5:1 for text, ≥3:1 for UI) meet WCAG AA
 * - Desaturated backgrounds minimize visual noise
 * - Semantic colors provide instant recognition
 * - Soft gradients create depth without overwhelm
 */

// Primary Colors - Calming Deep Blue
val CalmBlue50 = Color(0xFFE3F2FD)
val CalmBlue100 = Color(0xFFBBDEFB)
val CalmBlue200 = Color(0xFF90CAF9)
val CalmBlue300 = Color(0xFF64B5F6)
val CalmBlue400 = Color(0xFF42A5F5)
val CalmBlue500 = Color(0xFF2196F3) // Primary
val CalmBlue600 = Color(0xFF1E88E5)
val CalmBlue700 = Color(0xFF1976D2)
val CalmBlue800 = Color(0xFF1565C0)
val CalmBlue900 = Color(0xFF0D47A1)

// Secondary Colors - Soothing Purple
val FocusPurple50 = Color(0xFFF3E5F5)
val FocusPurple100 = Color(0xFFE1BEE7)
val FocusPurple200 = Color(0xFFCE93D8)
val FocusPurple300 = Color(0xFFBA68C8)
val FocusPurple400 = Color(0xFFAB47BC)
val FocusPurple500 = Color(0xFF9C27B0) // Secondary
val FocusPurple600 = Color(0xFF8E24AA)
val FocusPurple700 = Color(0xFF7B1FA2)
val FocusPurple800 = Color(0xFF6A1B9A)
val FocusPurple900 = Color(0xFF4A148C)

// Tertiary Colors - Energizing Teal
val EnergyTeal50 = Color(0xFFE0F2F1)
val EnergyTeal100 = Color(0xFFB2DFDB)
val EnergyTeal200 = Color(0xFF80CBC4)
val EnergyTeal300 = Color(0xFF4DB6AC)
val EnergyTeal400 = Color(0xFF26A69A)
val EnergyTeal500 = Color(0xFF009688) // Tertiary
val EnergyTeal600 = Color(0xFF00897B)
val EnergyTeal700 = Color(0xFF00796B)
val EnergyTeal800 = Color(0xFF00695C)
val EnergyTeal900 = Color(0xFF004D40)

// Semantic Colors
val SuccessGreen = Color(0xFF4CAF50)
val SuccessGreenLight = Color(0xFF81C784)
val WarningAmber = Color(0xFFFFA726)
val ErrorRed = Color(0xFFEF5350)
val InfoBlue = Color(0xFF42A5F5)

// Neutral Colors - Soft Grays
val NeutralGray50 = Color(0xFFFAFAFA)
val NeutralGray100 = Color(0xFFF5F5F5)
val NeutralGray200 = Color(0xFFEEEEEE)
val NeutralGray300 = Color(0xFFE0E0E0)
val NeutralGray400 = Color(0xFFBDBDBD)
val NeutralGray500 = Color(0xFF9E9E9E)
val NeutralGray600 = Color(0xFF757575)
val NeutralGray700 = Color(0xFF616161)
val NeutralGray800 = Color(0xFF424242)
val NeutralGray900 = Color(0xFF212121)

// Background Colors
val BackgroundLight = Color(0xFFF8F9FA)
val BackgroundDark = Color(0xFF121212)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)

// Light Theme - Soft, airy, minimal eye strain
val ADHDLightColorScheme = lightColorScheme(
    // Primary colors
    primary = CalmBlue700,
    onPrimary = Color.White,
    primaryContainer = CalmBlue100,
    onPrimaryContainer = CalmBlue900,

    // Secondary colors
    secondary = FocusPurple600,
    onSecondary = Color.White,
    secondaryContainer = FocusPurple100,
    onSecondaryContainer = FocusPurple900,

    // Tertiary colors
    tertiary = EnergyTeal600,
    onTertiary = Color.White,
    tertiaryContainer = EnergyTeal100,
    onTertiaryContainer = EnergyTeal900,

    // Error colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Background
    background = BackgroundLight,
    onBackground = NeutralGray900,

    // Surface
    surface = SurfaceLight,
    onSurface = NeutralGray900,
    surfaceVariant = NeutralGray100,
    onSurfaceVariant = NeutralGray700,

    // Outline
    outline = NeutralGray400,
    outlineVariant = NeutralGray200,

    // Inverse colors
    inverseSurface = NeutralGray800,
    inverseOnSurface = NeutralGray100,
    inversePrimary = CalmBlue300,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.32f),

    // Surface tint
    surfaceTint = CalmBlue700
)

// Dark Theme - Deep, calming, OLED-friendly
val ADHDDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = CalmBlue400,
    onPrimary = CalmBlue900,
    primaryContainer = CalmBlue800,
    onPrimaryContainer = CalmBlue100,

    // Secondary colors
    secondary = FocusPurple400,
    onSecondary = FocusPurple900,
    secondaryContainer = FocusPurple800,
    onSecondaryContainer = FocusPurple100,

    // Tertiary colors
    tertiary = EnergyTeal400,
    onTertiary = EnergyTeal900,
    tertiaryContainer = EnergyTeal800,
    onTertiaryContainer = EnergyTeal100,

    // Error colors
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Background
    background = BackgroundDark,
    onBackground = NeutralGray100,

    // Surface
    surface = SurfaceDark,
    onSurface = NeutralGray100,
    surfaceVariant = NeutralGray800,
    onSurfaceVariant = NeutralGray400,

    // Outline
    outline = NeutralGray600,
    outlineVariant = NeutralGray700,

    // Inverse colors
    inverseSurface = NeutralGray100,
    inverseOnSurface = NeutralGray900,
    inversePrimary = CalmBlue700,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.8f),

    // Surface tint
    surfaceTint = CalmBlue400
)

// Extended color palette for specific use cases
object ADHDColors {
    // Mission type colors
    val MissionMath = Color(0xFF5C6BC0) // Indigo
    val MissionBarcode = Color(0xFF26A69A) // Teal
    val MissionPhoto = Color(0xFF7E57C2) // Deep Purple
    val MissionActivity = Color(0xFFFF7043) // Deep Orange
    val MissionTyping = Color(0xFF66BB6A) // Green

    // Focus intensity colors
    val FocusGentle = Color(0xFF81C784) // Light Green
    val FocusModerate = Color(0xFFFFB74D) // Amber
    val FocusStrict = Color(0xFFE57373) // Light Red

    // Sleep quality colors
    val SleepExcellent = Color(0xFF66BB6A)
    val SleepGood = Color(0xFF9CCC65)
    val SleepFair = Color(0xFFFFCA28)
    val SleepPoor = Color(0xFFEF5350)

    // Gradient colors for backgrounds
    val GradientStart = CalmBlue50
    val GradientEnd = FocusPurple50
    val GradientStartDark = Color(0xFF1A237E)
    val GradientEndDark = Color(0xFF4A148C)
}
