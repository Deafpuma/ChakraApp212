package com.chakrawellness.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.chakrawellness.app.R
import com.chakrawellness.app.utility.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.layout.ContentScale

data class ChakraQuizData(
    val headerImage: Int,
    val chakraImage: Int,
    val nextChakraRoute: String?,
    val questions: List<String>,
    val chakraColor: Color
)



@Composable
fun QuizScreen(navController: NavHostController, chakraName: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val chakraData = getChakraAssets(chakraName)
    val answers = remember { mutableStateOf(mutableMapOf<String, Int>()) }
    val showValidationDialog = remember { mutableStateOf(false) }
    val unansweredQuestions = remember { mutableStateOf(emptyList<String>()) }

    // ✅ Enable Scrolling
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // ✅ This makes the screen scrollable
    ) {
        // ✅ Header with Logo, Background, and Name
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.header_background),
                contentDescription = "Header Background",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(id = R.drawable.sensory_oasis_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopCenter)
            )
        }

        // ✅ Chakra Background Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(chakraData.chakraColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chakraName.uppercase(),
                fontSize = 22.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            chakraData.questions.forEach { question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = question, fontSize = 18.sp)
                        Slider(
                            value = (answers.value[question] ?: 0).toFloat(),
                            onValueChange = { newValue ->
                                answers.value = answers.value.toMutableMap().apply { put(question, newValue.toInt()) }
                            },
                            valueRange = 0f..5f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = chakraData.chakraColor,
                                activeTrackColor = chakraData.chakraColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val missingQuestions = chakraData.questions.filter { it !in answers.value }
                    if (missingQuestions.isNotEmpty()) {
                        unansweredQuestions.value = missingQuestions
                        showValidationDialog.value = true
                    } else {
                        FirestoreUtils.saveQuizResults(userId, mapOf(chakraName to answers.value), onSuccess = {
                            navController.navigate(chakraData.nextChakraRoute ?: "chakraResults")
                        }, onFailure = {})
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = chakraData.chakraColor)
            ) {
                Text(if (chakraData.nextChakraRoute != null) "Next" else "Submit Quiz")
            }
        }
    }
}



fun getChakraAssets(chakraName: String): ChakraQuizData {
    return when (chakraName) {
        "Root" -> ChakraQuizData(
            R.drawable.header_bg_red, R.drawable.red_quiz, "quizSacral",
            listOf("Fatigue", "Insecurity", "Addictions", "Greed", "Instability", "Lacking a sense of identity"),
            Color(0xFF8B0000)
        )
        "Sacral" -> ChakraQuizData(
            R.drawable.header_bg_orange, R.drawable.orange_quiz, "quizSolarPlexus",
            listOf("Loneliness", "Addicted", "Betrayal", "Low libido", "Regretful", "Anxious", "Guilty", "Low back pain", "Urinary Problems", "Allergies"),
            Color(0xFFD2691E)
        )
        "Solar Plexus" -> ChakraQuizData(
            R.drawable.header_bg_yellow, R.drawable.yellow_quiz, "quizHeart",
            listOf("Low self-esteem", "Digestive issues", "Lack of confidence", "Fear", "Loss of control"),
            Color(0xFFDAA520)
        )
        "Heart" -> ChakraQuizData(
            R.drawable.header_bg_green, R.drawable.green_quiz, "quizThroat",
            listOf("Jealousy", "Abandonment", "Anger", "Bitterness", "Fear", "Rejection", "Envy and conditional love"),
            Color(0xFF228B22)
        )
        "Throat" -> ChakraQuizData(
            R.drawable.header_bg_blue, R.drawable.blue_quiz, "quizThirdEye",
            listOf("Insecurity", "Anxiety", "Fear of judgement", "Powerless to speak out"),
            Color(0xFF1E90FF)
        )
        "Third Eye" -> ChakraQuizData(
            R.drawable.header_bg_indigo, R.drawable.indigo_quiz, "quizCrown",
            listOf("Emotional", "Judgemental", "Unfocused", "Nightmares", "Poor memory", "Migraines"),
            Color(0xFF2A3F9D)
        )
        "Crown" -> ChakraQuizData(
            R.drawable.header_bg_violet, R.drawable.violet_quiz, null,
            listOf("Hopelessness", "Disconnected", "Rigid thoughts", "Depression and confusion"),
            Color(0xFF6A0DAD)
        )
        else -> ChakraQuizData(R.drawable.header_bg_red, R.drawable.red_quiz, "quizSacral", listOf("No questions available."), Color.Gray)
    }
}

