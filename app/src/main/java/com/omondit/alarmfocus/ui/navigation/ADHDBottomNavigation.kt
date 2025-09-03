package com.omondit.alarmfocus.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ADHDBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp) // Large touch targets
                )
            },
            label = {
                Text(
                    "Alarms",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = currentRoute == "alarms",
            onClick = { onNavigate("alarms") },
            modifier = Modifier.semantics {
                contentDescription = "Alarms tab"
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Task,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    "Missions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = currentRoute == "missions",
            onClick = { onNavigate("missions") },
            modifier = Modifier.semantics {
                contentDescription = "Wake-up missions tab"
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Block,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    "Focus",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = currentRoute == "focus",
            onClick = { onNavigate("focus") },
            modifier = Modifier.semantics {
                contentDescription = "Focus mode tab"
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    "Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            modifier = Modifier.semantics {
                contentDescription = "Settings tab"
            }
        )
    }
}