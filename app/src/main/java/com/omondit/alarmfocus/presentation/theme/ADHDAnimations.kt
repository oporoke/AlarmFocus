package com.omondit.alarmfocus.presentation.theme

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.dp

/**
 * ADHD-Optimized Animation System
 *
 * Design Rationale:
 * - Smooth, predictable motion reduces cognitive load
 * - Gentle easing curves feel natural and calming
 * - Moderate durations (300-400ms) are perceivable but not slow
 * - Consistent timing across app creates familiarity
 * - No jarring or sudden movements
 */

object ADHDAnimations {
    // Timing constants
    const val FAST = 200
    const val NORMAL = 300
    const val SLOW = 400
    const val EXTRA_SLOW = 600

    // Easing curves - All use gentle, natural motion
    val EaseInOutCubic = CubicBezierEasing(0.65f, 0.0f, 0.35f, 1.0f)
    val EaseOutQuart = CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f)
    val EaseInOutQuart = CubicBezierEasing(0.76f, 0.0f, 0.24f, 1.0f)

    // Spring configurations - Bouncy but controlled
    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val ResponsiveSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Tween animations
    fun <T> gentleTween(durationMillis: Int = NORMAL) = tween<T>(
        durationMillis = durationMillis,
        easing = EaseInOutCubic
    )

    fun <T> quickTween(durationMillis: Int = FAST) = tween<T>(
        durationMillis = durationMillis,
        easing = EaseOutQuart
    )

    // Shared element transitions
    val SharedElementTransition = tween<Float>(
        durationMillis = SLOW,
        easing = EaseInOutQuart
    )

    // Screen transitions
    val ScreenEnter = slideInVertically(
        initialOffsetY = { it / 3 },
        animationSpec = gentleTween(NORMAL)
    ) + fadeIn(animationSpec = gentleTween(NORMAL))

    val ScreenExit = slideOutVertically(
        targetOffsetY = { -it / 3 },
        animationSpec = gentleTween(NORMAL)
    ) + fadeOut(animationSpec = gentleTween(NORMAL))

    // Fade transitions
    val FadeIn = fadeIn(animationSpec = gentleTween(NORMAL))
    val FadeOut = fadeOut(animationSpec = gentleTween(NORMAL))

    // Scale animations for emphasis
    val PulseAnimation = infiniteRepeatable<Float>(
        animation = tween(1000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    )

    // Success/Error feedback animations
    val SuccessBounce = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val ErrorShake = keyframes<Float> {
        durationMillis = 400
        0f at 0
        -10f at 100
        10f at 200
        -5f at 300
        0f at 400
    }
}

// Haptic feedback intensities (for implementation with HapticFeedback API)
object ADHDHaptics {
    const val LIGHT = 0.3f // Gentle tap
    const val MEDIUM = 0.6f // Standard click
    const val HEAVY = 1.0f // Strong confirmation
}
