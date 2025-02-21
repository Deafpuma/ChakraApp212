package com.chakrawellness.app.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight // ✅ Fix for missing import
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chakrawellness.app.utility.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth

@Composable
fun QuizScreen(navController: NavController, onComplete: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var userId by remember { mutableStateOf("") }

    // ✅ Fetch user ID once when the screen loads
    LaunchedEffect(Unit) {
        userId = auth.currentUser?.uid.orEmpty()
    }

    val quizResponses = remember {
        mutableStateOf<Map<String, MutableMap<String, Int>>>(
            mutableMapOf(
                "Root" to mutableMapOf(),
                "Sacral" to mutableMapOf(),
                "Solar Plexus" to mutableMapOf(),
                "Heart" to mutableMapOf(),
                "Throat" to mutableMapOf(),
                "Third Eye" to mutableMapOf(),
                "Crown" to mutableMapOf()
            )
        )
    }


    val chakraQuestions = mapOf(
        "Root" to listOf("Fatigue", "Insecurity", "Instability"),
        "Sacral" to listOf("Loneliness", "Betrayal", "Regretful"),
        "Solar Plexus" to listOf("Low self-esteem", "Fear", "Lack of confidence"),
        "Heart" to listOf("Jealousy", "Abandonment", "Anger"),
        "Throat" to listOf("Anxiety", "Fear of judgement"),
        "Third Eye" to listOf("Judgemental", "Unfocused"),
        "Crown" to listOf("Hopelessness", "Disconnected")
    )

    var showDialog by remember { mutableStateOf(false) }
    var missingQuestions by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // ✅ Enables scrolling
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chakra Quiz", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        chakraQuestions.forEach { (chakra, questions) ->
            Text(text = chakra, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
            questions.forEach { question ->
                ChakraSlider(question, chakra, quizResponses)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val unansweredQuestions = mutableListOf<String>()

                quizResponses.value.forEach { (chakra, responses) ->
                    responses.forEach { (question, answer) ->
                        if (answer == 0) { // Check if answer is left at default (0)
                            unansweredQuestions.add("$chakra - $question")
                        }
                    }
                }

                if (unansweredQuestions.isNotEmpty()) {
                    showDialog = true
                    missingQuestions = unansweredQuestions.joinToString("\n") // Convert to readable format
                } else {
                    submitQuiz(userId, quizResponses.value, navController, onComplete) // ✅ Pass userId correctly
                }
            }
        ) {
            Text("Submit Quiz")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Incomplete Quiz", fontWeight = FontWeight.Bold) },
            text = { Text("You left some questions blank. Please answer:\n\n$missingQuestions") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    submitQuiz(userId, quizResponses.value, navController, onComplete) // ✅ Pass userId correctly
                }) {
                    Text("Yes, Submit")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Go Back")
                }
            }
        )
    }
}

@Composable
fun ChakraSlider(
    question: String,
    chakra: String,
    quizResponses: MutableState<Map<String, MutableMap<String, Int>>>
) {
    var sliderValue by remember { mutableStateOf(1f) } // ✅ Default starts at 1 to avoid "unanswered" issues

    val labels = listOf("None", "Mild", "Moderate", "Severe", "Extreme") // ✅ Labels for better clarity

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = question, style = MaterialTheme.typography.bodyMedium)

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    quizResponses.value = quizResponses.value.toMutableMap().apply {
                        this[chakra]?.set(question, it.toInt())
                    }
                },
                valueRange = 1f..5f,  // ✅ Starts at 1, so no "zero" issues
                steps = 3,  // ✅ Evenly spaced steps
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ✅ Labels under the slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // ✅ Bubble that shows selected value
        Text("Selected: ${labels[sliderValue.toInt() - 1]}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun submitQuiz(
    userId: String,
    quizResponses: Map<String, Map<String, Int>>,
    navController: NavController,
    onComplete: () -> Unit
) {
    if (userId.isEmpty()) {
        Log.e("QuizScreen", "User not authenticated")
        return
    }

    FirestoreUtils.saveQuizResults(  // ✅ Ensure userId is passed
        userId = userId,
        quizResults = quizResponses,
        onSuccess = {
            Log.d("QuizScreen", "Quiz results saved successfully")
            onComplete()
            navController.navigate("quizResults") // ✅ Navigate to results screen
        },
        onFailure = { error ->
            Log.e("QuizScreen", "Failed to save quiz results: $error")
        }
    )
}
