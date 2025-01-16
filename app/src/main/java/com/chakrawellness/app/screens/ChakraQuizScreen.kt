package com.chakrawellness.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChakraQuizScreen(
    onSignInClick: () -> Unit,
    onSignInWithGoogleClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onSubmitQuiz: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to Chakra Quiz!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSignInClick) {
            Text("Sign In")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSignInWithGoogleClick) {
            Text("Sign In with Google")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateAccountClick) {
            Text("Create Account")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSubmitQuiz) {
            Text("Submit Quiz")
        }
    }
}

