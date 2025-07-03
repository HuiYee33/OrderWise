package com.example.orderwise2.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class AdminBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object DailySales : AdminBottomNavItem(
        route = Screen.AdminDashboard.route,
        title = "Daily Sales",
        icon = Icons.Default.BarChart
    )
    object MenuManagement : AdminBottomNavItem(
        route = Screen.AdminMenu.route,
        title = "Menu",
        icon = Icons.Default.Restaurant
    )
    object Reviews : AdminBottomNavItem(
        route = Screen.AdminReview.route,
        title = "Reviews",
        icon = Icons.Default.Star
    )
    object Profile : AdminBottomNavItem(
        route = Screen.AdminCafeProfile.route,
        title = "Profile",
        icon = Icons.Default.Person
    )
}

@Composable
fun AdminBottomNavigation(navController: NavController) {
    val items = listOf(
        AdminBottomNavItem.DailySales,
        AdminBottomNavItem.MenuManagement,
        AdminBottomNavItem.Reviews,
        AdminBottomNavItem.Profile
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
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
} 