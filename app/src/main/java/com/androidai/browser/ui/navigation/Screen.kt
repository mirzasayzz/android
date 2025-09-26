package com.androidai.browser.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    object Explore : Screen("explore", "Explore", Icons.Default.Explore)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
} 