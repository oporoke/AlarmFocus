package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omondit.alarmfocus.presentation.theme.ADHDCustomShapes

/**
 * D15: Device Admin Onboarding Screen
 *
 * Educational screen explaining why device admin protection is beneficial
 * for ADHD users trying to build reliable alarm habits.
 */
@Composable
fun DeviceAdminOnboardingScreen(
    onEnableDeviceAdmin: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Security icon
        Icon(
            Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Alarm Protection",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle
        Text(
            text = "Why do we need Device Admin?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Explanation
        Text(
            text = """
                This optional feature prevents accidental app uninstall when you're trying to skip alarms.

                • Ensures alarms work reliably
                • Prevents impulsive decisions during sleepy mornings
                • 24-hour cooldown for deactivation
                • You remain in full control

                You can always disable this in Settings → Security.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Enable button
        Button(
            onClick = onEnableDeviceAdmin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = ADHDCustomShapes.PillButton,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "Enable Protection",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skip button
        TextButton(
            onClick = onSkip,
            modifier = Modifier.height(48.dp)
        ) {
            Text("Skip for Now")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Additional info
        Text(
            text = "This is ADHD-friendly protection, not app lock malware",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Preview for DeviceAdminOnboardingScreen
 */
@Composable
fun DeviceAdminOnboardingPreview() {
    MaterialTheme {
        DeviceAdminOnboardingScreen(
            onEnableDeviceAdmin = {},
            onSkip = {}
        )
    }
}
