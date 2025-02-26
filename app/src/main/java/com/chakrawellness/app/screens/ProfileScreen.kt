package com.chakrawellness.app.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chakrawellness.app.models.UserProfile
import com.chakrawellness.app.utility.FirestoreUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String
) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    val quizResults = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ Load user profile and latest quiz results
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        errorMessage = "Failed to load profile: ${error.message}"
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (document != null && document.exists()) {
                        userProfile = document.toObject<UserProfile>()
                    } else {
                        errorMessage = "Profile not found."
                    }
                    isLoading = false
                }

            // ✅ Fetch quiz results
            FirestoreUtils.getQuizResults(
                userId = userId,
                onSuccess = { results ->
                    quizResults.value = results.map { result ->
                        mapOf(
                            "quizResults" to result.quizResults, // ✅ No need for `as?`
                            "timestamp" to result.timestamp
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("ProfileScreen", "Failed to load quiz scores: $error")
                }
            )
        } else {
            errorMessage = "Invalid user ID"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading profile...", color = Color.Gray)
                }
                userProfile != null -> {
                    val profile = userProfile!!
                    Text("Name: ${profile.name}")
                    Text("Age: ${profile.age}")
                    Text("Weight: ${profile.weight} lbs")
                    Text("Height: ${profile.heightFeet} ft ${profile.heightInches} in")

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Most Recent Quiz:", fontSize = 18.sp, color = Color.Blue)

                    if (quizResults.value.isNotEmpty()) {
                        val latestQuiz = quizResults.value.lastOrNull() ?: emptyMap()
                        val previousQuiz = if (quizResults.value.size > 1) quizResults.value[quizResults.value.size - 2] else null
                        val chakraScores = extractChakraScores(latestQuiz)
                        val previousScores = previousQuiz?.let { extractChakraScores(it) }

                        chakraScores?.forEach { (chakra, score) ->
                            val previousScore = previousScores?.get(chakra) ?: 0
                            val difference = score - previousScore

                            Text(
                                text = "$chakra: $score (${formatChange(difference)})",
                                fontSize = 14.sp,
                                color = when {
                                    difference > 0 -> Color.Red
                                    difference < 0 -> Color.Green
                                    else -> Color.Black
                                }
                            )
                        }
                    } else {
                        Text("No quiz results found.")
                    }
                }
            }
        }
    }
}


// ✅ Extract individual chakra scores
fun extractChakraScores(quizResult: Map<String, Any>): Map<String, Int> {
    val quizData = quizResult["quizResults"] as? Map<String, Map<String, Int>> ?: return emptyMap()
    return quizData.mapValues { it.value.values.sum() }  // Sum up scores per chakra
}

// ✅ Format score change for readability
fun formatChange(value: Int): String {
    return when {
        value > 0 -> "↓ Worse by $value"
        value < 0 -> "↑ Improved by ${-value}"
        else -> "No change"
    }
}
