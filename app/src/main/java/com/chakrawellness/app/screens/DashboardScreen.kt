package com.chakrawellness.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DashboardScreen(
    navController: NavController,
    isProfileCreated: Boolean,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Button to view or create profile
        Button(
            onClick = {
                if (isProfileCreated) {
                    navController.navigate("profile")
                } else {
                    navController.navigate("createProfile")
                }
            }
        ) {
            Text(if (isProfileCreated) "View Profile" else "Create Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout button
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}
