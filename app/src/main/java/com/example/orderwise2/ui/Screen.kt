package com.example.orderwise2.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MenuHome : Screen("menu_home")
    object EditIngredients : Screen("edit_ingredients")
    object Cart : Screen("cart")
    object OrderLater : Screen("order_later")
    object PaymentSuccess : Screen("payment_success")
    object Profile : Screen("profile")
    object PurchaseHistory : Screen("purchase_history")
    object Receipt : Screen("receipt")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminMenu : Screen("admin_menu")
    object AdminReview : Screen("admin_review")
    object AdminCafeProfile : Screen("admin_cafe_profile")
    object AdminVoucher : Screen("admin_voucher")
    object ManageVoucher : Screen("manage_voucher")
    object RedeemVoucher : Screen("redeem_voucher")
    object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: String) = "food_detail/$foodId"
    }
    object Login : Screen("login")
    object PaymentMethod : Screen("payment_method")
    object DateTimeSelection : Screen("date_time_selection")
} 