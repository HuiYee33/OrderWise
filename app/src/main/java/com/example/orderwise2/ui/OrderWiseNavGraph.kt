package com.example.orderwise2.ui

import android.util.Log
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
import com.example.orderwise2.ui.LoginScreen
import com.example.orderwise2.ui.AdminDashboardScreen
import com.example.orderwise2.ui.AdminMenuScreen
import com.example.orderwise2.ui.AdminReviewScreen
import com.example.orderwise2.ui.AdminCafeProfileScreen
import com.example.orderwise2.ui.HomeScreen
import com.example.orderwise2.ui.CompleteProfileScreen
// Add admin screens imports here if needed

@Composable
fun OrderWiseNavGraph(navController: NavHostController, startDestination: String) {
    val cartViewModel: CartViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.MenuHome.route) {
            MenuHomeScreen(navController)
        }
        composable(Screen.EditIngredients.route) { /* TODO: Implement or remove */ }
        composable(Screen.Cart.route) {
            CartScreen(
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
        composable(Screen.OrderLater.route) {
            OrderLaterScreen(
                navController = navController,
                cartItems = cartViewModel.cartItems,
                cartViewModel = cartViewModel
            )
        }
        composable(Screen.PaymentSuccess.route) { PaymentSuccessScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.PurchaseHistory.route) {
            PurchaseHistoryScreen(navController, cartViewModel)
        }
        composable(Screen.Receipt.route) {
            val latestPurchase = cartViewModel.purchaseHistory.lastOrNull()
            if (latestPurchase != null) {
                ReceiptScreen(navController, latestPurchase)
            } else {
                ReceiptScreen(navController, null)
            }
        }
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
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen(navController) }
        composable(Screen.AdminMenu.route) { AdminMenuScreen(navController) }
        composable(Screen.AdminReview.route) { AdminReviewScreen(navController) }
        composable(Screen.AdminCafeProfile.route) { AdminCafeProfileScreen(navController) }
        // Add complete profile route
        composable("complete_profile") { CompleteProfileScreen(navController) }
    }
} 