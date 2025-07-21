package com.example.orderwise2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.orderwise2.ui.OrderWiseNavGraph
import com.example.orderwise2.ui.Screen
import com.example.orderwise2.ui.theme.OrderWise2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.orderwise2.ui.CloudinaryManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryManager.init(applicationContext)
        setContent {
            OrderWise2Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    // Check if user is authenticated
                    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
                    val startDestination = if (isLoggedIn) {
                        com.example.orderwise2.ui.Screen.Home.route
                    } else {
                        com.example.orderwise2.ui.Screen.Login.route
                    }
                    com.example.orderwise2.ui.OrderWiseNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}