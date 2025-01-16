package com.chakrawellness.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


import androidx.compose.foundation.Image
import androidx.compose.material3.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

@Composable
fun HomeScreen(
    userId: String,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var profileData by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch profile data
    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                profileData = document.toObject<UserProfile>()
                loading = false
            }
            .addOnFailureListener { exception ->
                errorMessage = "Failed to load profile: ${exception.message}"
                loading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            Text("Loading profile...", style = MaterialTheme.typography.bodyLarge)
        } else if (errorMessage != null) {
            Text(errorMessage ?: "Unknown error", color = Color.Red)
        } else {
            profileData?.let { profile ->
                Text("Welcome, ${profile.name}!", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                // Display profile picture
                if (profile.profilePicture.isNotEmpty()) {
                    Image(
                        painter =
                        rememberAsyncImagePainter(profile.profilePicture),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display other profile details
                Text("Age: ${profile.age}")
                Text("Weight: ${profile.weight} lbs")
                Text("Height: ${profile.heightFeet}'${profile.heightInches}\"")

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onEditProfile) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}

// Data class for UserProfile
data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val weight: Int = 0,
    val heightFeet: Int = 0,
    val heightInches: Int = 0,
    val profilePicture: String = ""
)
