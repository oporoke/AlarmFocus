package com.omondit.alarmfocus.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ADHD-Optimized Typography System
 *
 * Design Rationale:
 * - Rounded, friendly fonts reduce reading stress
 * - Larger base sizes (16sp+) improve readability
 * - Generous line height (1.5x) prevents text crowding
 * - Bold weights for emphasis without being harsh
 * - Letter spacing optimized for dyslexia-friendly reading
 */

// Font families - Using system defaults with optimal settings
// In production, consider: Lexend, Atkinson Hyperlegible, or Open Dyslexic
val ADHDFontFamily = FontFamily.Default

val ADHDTypography = Typography(
    // Display styles - Large, impactful headers
    displayLarge = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles - Section headers
    headlineLarge = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles - Card headers, dialog titles
    titleLarge = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp, // Increased from 16sp for ADHD
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp, // Increased from 14sp
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - Main content
    bodyLarge = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // Increased from 16sp
        lineHeight = 27.sp, // 1.5x line height
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // Increased from 14sp
        lineHeight = 24.sp, // 1.5x line height
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // Increased from 12sp
        lineHeight = 21.sp, // 1.5x line height
        letterSpacing = 0.4.sp
    ),

    // Label styles - Buttons, tabs, chips
    labelLarge = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, // Increased from 14sp
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // Increased from 12sp
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, // Increased from 11sp
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Accessibility text styles for extra emphasis
object ADHDTextStyles {
    // Extra large for critical information (alarms, time displays)
    val TimeDisplay = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-1).sp
    )

    // Mission instructions - very clear
    val MissionInstruction = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.15.sp
    )

    // Success/Error messages - highly visible
    val FeedbackMessage = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.5.sp
    )

    // Calm, reassuring text
    val CalmText = TextStyle(
        fontFamily = ADHDFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 28.sp, // Extra generous line height
        letterSpacing = 1.sp // Extra letter spacing for calm reading
    )
}
