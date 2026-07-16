package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { SettingsSectionHeader("Communication") }
            item { SettingsItem("Blocked Numbers", "Manage blocked contacts", onClick = { navController.navigate(Screen.BlockedNumbers.route) }) }
            item { SettingsItem("Incoming Call Gestures", "Flip to silence, raise to answer", onClick = { }) }
            item { SettingsItem("Call Forwarding Settings", "Configure call forwarding", onClick = { navController.navigate(Screen.CallForwarding.route) }) }
            
            item { SettingsSectionHeader("Appearance") }
            item { SettingsItem("Theme Appearance", "System default, Light, Dark", onClick = { navController.navigate(Screen.ThemeAppearance.route) }) }
            
            item { SettingsSectionHeader("Security") }
            item { SettingsItem("AI Spam Detection", "Protect against spam and fraud calls", onClick = { navController.navigate(Screen.SpamDetection.route) }) }
            
            item { SettingsSectionHeader("About") }
            item { SettingsItem("About App", "Version, Licenses, Terms", onClick = { navController.navigate(Screen.About.route) }) }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp, end = 16.dp)
    )
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
