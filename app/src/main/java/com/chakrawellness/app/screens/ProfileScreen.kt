package com.chakrawellness.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onCreateProfile: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Your Profile", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Button to Create/Edit Profile
        Button(onClick = onCreateProfile) {
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}