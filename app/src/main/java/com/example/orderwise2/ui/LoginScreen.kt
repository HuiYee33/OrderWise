package com.example.orderwise2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.orderwise2.R
import androidx.compose.ui.graphics.Color

@Composable
fun LoginScreen(navController: NavController) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

    val context = LocalContext.current
    val activity = context as? Activity

    // TODO: Replace with your actual web client ID from Firebase Console
    val webClientId = "1068919815592-dmloavrch01uahvhrl9iaaddv57hov5c.apps.googleusercontent.com"

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isLoading = true
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    isLoading = false
                    if (authResult.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val email = user?.email
                        val adminEmail = "huiyee.khor@qiu.edu.my" // TODO: Replace with your admin Gmail address
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        if (email == adminEmail) {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else if (email != null) {
                            db.collection("users").document(email).get()
                                .addOnSuccessListener { doc ->
                                    val username = doc.getString("username")
                                    val phone = doc.getString("phone")
                                    if (username.isNullOrBlank() || phone.isNullOrBlank()) {
                                        navController.navigate("complete_profile") {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Screen.MenuHome.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                        }
                    } else {
                        errorMessage = authResult.exception?.message
                    }
                }
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    fun handleAuth() {
        isLoading = true
        errorMessage = null
        if (isLogin) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        // Navigate to home or main screen
                        navController.navigate(Screen.MenuHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        errorMessage = task.exception?.message
                    }
                }
        } else {
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match."
                isLoading = false
                return
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        navController.navigate(Screen.MenuHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        errorMessage = task.exception?.message
                    }
                }
        }
    }

    fun sendPasswordReset() {
        if (email.isBlank()) {
            errorMessage = "Enter your email to reset password."
            return
        }
        isLoading = true
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    errorMessage = "Password reset email sent."
                } else {
                    errorMessage = task.exception?.message
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(0.dp))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterHorizontally)
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Text("Log In", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(50.dp))
            Button(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text("Sign in with Google")
            }
            if (isLoading) {
                Spacer(Modifier.height(86.dp))
                CircularProgressIndicator()
            }
        }
        Spacer(Modifier.height(0.dp))
    }
} 