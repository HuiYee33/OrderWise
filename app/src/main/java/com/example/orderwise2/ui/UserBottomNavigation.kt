package com.example.orderwise2.ui

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination

sealed class UserBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : UserBottomNavItem(
        route = Screen.Home.route,
        title = "Home",
        icon = Icons.Default.Home
    )
    object Menu : UserBottomNavItem(
        route = Screen.MenuHome.route,
        title = "Menu",
        icon = Icons.Default.Menu
    )
    object Cart : UserBottomNavItem(
        route = Screen.Cart.route,
        title = "Cart",
        icon = Icons.Default.ShoppingCart
    )
    object Profile : UserBottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        icon = Icons.Default.Person
    )
}

@Composable
fun UserBottomNavigation(navController: NavController) {
    val items = listOf(
        UserBottomNavItem.Home,
        UserBottomNavItem.Menu,
        UserBottomNavItem.Cart,
        UserBottomNavItem.Profile
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == Screen.Home.route) {
                        Log.d("UserBottomNavigation", "Home button clicked")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
} 