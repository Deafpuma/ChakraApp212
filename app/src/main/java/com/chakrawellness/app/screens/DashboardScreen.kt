package com.chakrawellness.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    isProfileCreated: Boolean, // Indicates whether the profile is created
    onLogout: () -> Unit, // Callback for logout action
    onCreateProfileClick: () -> Unit // Callback for creating a profile
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Logout")
        }

        // Conditional UI based on `isProfileCreated`
        if (!isProfileCreated) {
            Button(
                onClick = onCreateProfileClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Profile")
            }
        } else {
            Text(
                text = "Profile already created!",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
