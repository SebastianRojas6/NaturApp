package com.example.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.app.ue.PostsScreen
import com.example.app.ue.ProductsScreen
import com.example.app.ue.SettingsScreen
import com.example.app.ue.TodoScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentRoute = currentDestination?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF512DA8)) {
                listOf(
                    Triple("tasks", "Tareas", Icons.Default.CheckCircle),
                    Triple("posts", "Posts", Icons.Default.List),
                    Triple("products", "Productos", Icons.Default.ShoppingCart),
                    Triple("settings", "Config", Icons.Default.Settings)
                ).forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = { navController.navigate(route) },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f),
                            indicatorColor = Color(0xFF7C4DFF)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "tasks",
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {

        composable("tasks") { TodoScreen() }
            composable("posts") { PostsScreen() }
            composable("products") { ProductsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}