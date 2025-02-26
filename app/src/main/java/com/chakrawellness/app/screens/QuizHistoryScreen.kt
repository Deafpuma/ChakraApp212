package com.chakrawellness.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chakrawellness.app.utility.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun QuizHistoryScreen(navController: NavController) {
    var quizResults by remember { mutableStateOf<List<Map<String, Any>>?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            FirestoreUtils.getQuizResults(
                userId = userId,  // ✅ Pass the user ID
                onSuccess = { results ->
                    quizResults = results.map { quizResult ->
                        mapOf(
                            "quizResults" to quizResult.quizResults,
                            "timestamp" to quizResult.timestamp
                        )
                    }
                },
                onFailure = { error ->
                    println("Error fetching quiz history: $error")
                }
            )
        } else {
            println("Error: No authenticated user found.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz History") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Past Quiz Scores", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            quizResults?.forEachIndexed { index, result ->
                val chakraScores = extractChakraScores(result)
                Text("Quiz #${index + 1}:", fontSize = 18.sp)
                chakraScores.forEach { (chakra, score) ->
                    Text("$chakra: $score", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            } ?: Text("No quiz results found.")
        }
    }
}
