package com.androidai.browser.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.androidai.browser.ui.screens.ChatScreen
import com.androidai.browser.ui.screens.ExploreScreen
import com.androidai.browser.ui.screens.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Chat.route) {
        composable(Screen.Chat.route) {
            ChatScreen()
        }
        composable(Screen.Explore.route) {
            ExploreScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
} 