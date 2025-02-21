package com.chakrawellness.app.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    navController: NavController,
    onGoogleSignInClick: () -> Unit,  // ✅ Added this missing parameter
    onCreateAccountClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid.orEmpty()
                        if (userId.isNotEmpty()) {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            errorMessage = "Login failed: Invalid user ID."
                        }
                    } else {
                        errorMessage = task.exception?.localizedMessage
                            ?: "Login failed: Unknown error."
                    }
                }
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Google Sign-In Button
        Button(onClick = onGoogleSignInClick) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onCreateAccountClick) {
            Text("Create Account")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }
    }
}
