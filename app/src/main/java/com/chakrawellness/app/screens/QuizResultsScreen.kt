package com.chakrawellness.app.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuizResultsScreen(navController: NavController, quizResults: Map<String, Any>) {
    val coroutineScope = rememberCoroutineScope()
    var mostOutOfBalanceChakra by remember { mutableStateOf("None") }
    var chakraScores by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(quizResults) {
        Log.d("QuizResultsScreen", "Raw Quiz Results: $quizResults")

        // ✅ Ensure Firestore timestamps are ignored
        val cleanedResults = quizResults.filterValues { it !is com.google.firebase.Timestamp }
            .mapValues { (_, responses) ->
                (responses as? Map<String, Any>)?.values?.mapNotNull { it as? Number }
                    ?.sumOf { it.toInt() } ?: 0
            }

        chakraScores = cleanedResults

        // ✅ Identify the most imbalanced chakra
        mostOutOfBalanceChakra = cleanedResults.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key ?: "None"

        Log.d("QuizResultsScreen", "Computed Scores: $cleanedResults")
        Log.d("QuizResultsScreen", "Most imbalanced chakra: $mostOutOfBalanceChakra")

        // ✅ Ensure navigation back to Profile happens on the main thread
        coroutineScope.launch {
            delay(5000)
            navController.navigate("profile") {
                popUpTo("quizResults") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your Chakra Balance Results", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        chakraScores.forEach { (chakra, score) ->
            Text("$chakra Chakra Score: $score", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Your most imbalanced chakra: $mostOutOfBalanceChakra",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate("profile") }) {
            Text("Go to Profile")
        }
    }
}
