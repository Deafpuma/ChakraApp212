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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryScreen(navController: NavController) {
    var quizResults by remember { mutableStateOf<List<Map<String, Any>>?>(null) }

    LaunchedEffect(Unit) {
        FirestoreUtils.getQuizResults(
            onSuccess = { results -> quizResults = results },
            onFailure = { error -> println("Error fetching quiz history: $error") }
        )
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
