package com.omondit.alarmfocus.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * ADHD-Optimized Shape System
 *
 * Design Rationale:
 * - Rounded corners create friendly, approachable feel
 * - Larger radii reduce visual sharpness/anxiety
 * - Consistent corner rounding across components
 * - Gradual size progression for hierarchy
 */

val ADHDShapes = Shapes(
    // Extra small - Chips, small badges
    extraSmall = RoundedCornerShape(8.dp),

    // Small - Small buttons, text fields
    small = RoundedCornerShape(12.dp),

    // Medium - Cards, dialogs, main buttons
    medium = RoundedCornerShape(16.dp),

    // Large - Bottom sheets, large cards
    large = RoundedCornerShape(24.dp),

    // Extra large - Full screen modals
    extraLarge = RoundedCornerShape(32.dp)
)

// Custom shapes for specific use cases
object ADHDCustomShapes {
    // Alarm cards - highly rounded for calm feel
    val AlarmCard = RoundedCornerShape(20.dp)

    // Mission screens - top rounded only
    val MissionScreen = RoundedCornerShape(
        topStart = 32.dp,
        topEnd = 32.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Bottom navigation - subtle top curve
    val BottomNav = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Focus mode cards - asymmetric for visual interest
    val FocusCard = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 24.dp
    )

    // Pill-shaped buttons
    val PillButton = RoundedCornerShape(100.dp)

    // Circular elements
    val Circle = RoundedCornerShape(50)

    // Time picker - extra soft
    val TimePicker = RoundedCornerShape(28.dp)
}
