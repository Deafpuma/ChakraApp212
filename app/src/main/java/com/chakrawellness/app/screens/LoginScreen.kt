package com.chakrawellness.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login to Chakra App")

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(onClick = onLoginSuccess) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Account Button
        Button(onClick = onCreateAccountClick) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In Button
        Button(onClick = onGoogleSignIn) {
            Text("Sign in with Google")
        }
    }
}
