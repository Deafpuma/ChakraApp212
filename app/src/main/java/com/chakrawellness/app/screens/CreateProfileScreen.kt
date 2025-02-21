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
import com.chakrawellness.app.utility.FirestoreUtils
import com.chakrawellness.app.models.UserProfile
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CreateProfileScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current

    // State variables for profile fields
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var heightFeet by remember { mutableStateOf("") }
    var heightInches by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Profile")
        Spacer(modifier = Modifier.height(16.dp))

        // Input fields for profile data
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (lbs)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = heightFeet,
            onValueChange = { heightFeet = it },
            label = { Text("Height (feet)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = heightInches,
            onValueChange = { heightInches = it },
            label = { Text("Height (inches)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error message display
        errorMessage?.let {
            Text(it, color = androidx.compose.ui.graphics.Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            if (name.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty() && heightFeet.isNotEmpty() && heightInches.isNotEmpty()) {
                val profile = UserProfile(
                    name = name,
                    age = age.toIntOrNull() ?: 0,
                    weight = weight.toIntOrNull() ?: 0,
                    heightFeet = heightFeet.toIntOrNull() ?: 0,
                    heightInches = heightInches.toIntOrNull() ?: 0
                )

                // Save the profile to Firestore
                FirestoreUtils.saveUserProfile(userId, profile,
                    onSuccess = {
                        Toast.makeText(context, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                        navController.navigate("profile") {
                            popUpTo("createProfile") { inclusive = true }
                        }
                    },
                    onFailure = { error ->
                        Log.e("CreateProfile", "Error saving profile: $error")
                        errorMessage = "Failed to save profile: $error"
                    }
                )
            } else {
                errorMessage = "All fields are required!"
            }
        }) {
            Text("Save Profile")
        }
    }
}
