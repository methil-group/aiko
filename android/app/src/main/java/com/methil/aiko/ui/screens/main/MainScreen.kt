package com.methil.aiko.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.methil.aiko.ui.navigation.AikoScreen
import com.methil.aiko.ui.theme.DarkPurple
import com.methil.aiko.ui.theme.LightestPink

@Composable
fun MainScreen(
    sessionToken: String,
    mainViewModel: com.methil.aiko.ui.viewmodels.MainViewModel,
    onCharacterSelect: (com.methil.aiko.domain.Character) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkPurple,
                contentColor = LightestPink,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                AikoScreen.items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 32.dp)
                                    .background(
                                        if (selected) LightestPink else Color.Transparent,
                                        RectangleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    screen.icon, 
                                    contentDescription = null,
                                    tint = if (selected) DarkPurple else LightestPink.copy(alpha = 0.6f)
                                )
                            }
                        },
                        label = { Text(stringResource(screen.titleRes), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkPurple,
                            selectedTextColor = LightestPink,
                            unselectedIconColor = LightestPink.copy(alpha = 0.6f),
                            unselectedTextColor = LightestPink.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent // Hide the default round indicator
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AikoScreen.Characters.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AikoScreen.Characters.route) {
                CharactersScreen(
                    sessionToken = sessionToken,
                    mainViewModel = mainViewModel,
                    onCharacterSelect = onCharacterSelect
                )
            }
            composable(AikoScreen.Profile.route) {
                ProfileScreen(
                    sessionToken = sessionToken,
                    onLogout = onLogout
                )
            }
            composable(AikoScreen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
