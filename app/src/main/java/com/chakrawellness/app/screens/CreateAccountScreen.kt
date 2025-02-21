package com.chakrawellness.app.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser

@Composable
fun CreateAccountScreen(
    navController: NavController,
    onAccountCreated: (FirebaseUser?) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error message
        errorMessage?.let {
            Text(it, color = androidx.compose.ui.graphics.Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val userId = user?.uid.orEmpty()

                                // Initialize Firestore entry for the user
                                db.collection("Users").document(userId).set(hashMapOf("profileCreated" to false))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                        onAccountCreated(user)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("CreateAccount", "Error initializing profile: ${e.message}")
                                        errorMessage = "Failed to create user profile: ${e.message}"
                                    }
                            } else {
                                errorMessage = "Account creation failed: ${task.exception?.message}"
                            }
                        }
                } else {
                    errorMessage = "Passwords do not match!"
                }
            } else {
                errorMessage = "All fields are required!"
            }
        }) {
            Text("Create Account")
        }
    }
}
