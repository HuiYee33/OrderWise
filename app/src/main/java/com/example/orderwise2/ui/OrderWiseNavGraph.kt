package com.example.orderwise2.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.orderwise2.ui.MenuHomeScreen
import com.example.orderwise2.ui.FoodDetailScreen
import com.example.orderwise2.ui.CartScreen
import com.example.orderwise2.ui.OrderLaterScreen
import com.example.orderwise2.ui.PaymentSuccessScreen
import com.example.orderwise2.ui.ProfileScreen
import com.example.orderwise2.ui.PurchaseHistoryScreen
import com.example.orderwise2.ui.ReceiptScreen
// Add admin screens imports here if needed

@Composable
fun OrderWiseNavGraph(navController: NavHostController) {
    val cartViewModel: CartViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = Screen.MenuHome.route) {
        composable(Screen.MenuHome.route) { MenuHomeScreen(navController) }
        composable(Screen.EditIngredients.route) { /* TODO: Implement or remove */ }
        composable(Screen.Cart.route) { CartScreen(navController, cartViewModel) }
        composable(Screen.OrderLater.route) { OrderLaterScreen(navController) }
        composable(Screen.PaymentSuccess.route) { PaymentSuccessScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.PurchaseHistory.route) { PurchaseHistoryScreen(navController) }
        composable(Screen.Receipt.route) { ReceiptScreen(navController) }
        // Admin screens here
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: "shrimp_fried_rice"
            FoodDetailScreen(navController, cartViewModel, foodId)
        }
    }
} 