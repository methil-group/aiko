package com.methil.aiko.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

import com.methil.aiko.R

sealed class AikoScreen(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector
) {
    object Characters : AikoScreen("characters", R.string.nav_characters, Icons.AutoMirrored.Filled.List)
    object Profile : AikoScreen("profile", R.string.nav_profile, Icons.Default.Person)
    object Settings : AikoScreen("settings", R.string.nav_settings, Icons.Default.Settings)

    companion object {
        val items = listOf(
            Characters,
            Profile,
            Settings
        )
    }
}
