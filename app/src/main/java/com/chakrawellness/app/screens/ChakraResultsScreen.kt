package com.chakrawellness.app.screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.chakrawellness.app.R
import com.chakrawellness.app.utility.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChakraResultsScreen(navController: NavHostController, chakraData: Map<String, Map<String, Int>> = emptyMap()) {
    val quizResults = remember { mutableStateOf(chakraData) }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid.orEmpty()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            FirestoreUtils.getLatestQuizResults(userId) { results ->
                if (results.isNotEmpty()) {
                    quizResults.value = results
                } else {
                    println("Error: No quiz results found.")
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ✅ Add Header with Background and Home Button
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
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
            ) {
                Text("Home")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.chakra_silhouette),
                contentDescription = "Chakra Silhouette",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                quizResults.value.forEach { (chakra, scores) ->
                    val maxPossibleScore = getMaxScoreForChakra(chakra) // Get total possible score for that chakra
                    val actualScore = scores.values.sum() // Sum of all answers

                    val percentage = if (maxPossibleScore > 0) {
                        ((actualScore.toFloat() / maxPossibleScore.toFloat()) * 100).toInt()
                    } else 0

                    Log.d("ChakraResults", "Chakra: $chakra, Score: $actualScore, Percentage: $percentage") // Debugging

                    ChakraBubble(percentage, getChakraColor(chakra), getChakraOffset(chakra))
                }

            }
        }
    }
}

fun getMaxScoreForChakra(chakraName: String): Int {
    return when (chakraName) {
        "Root" -> 6 * 5  // 6 questions, each max 5 points
        "Sacral" -> 10 * 5 // 10 questions
        "Solar Plexus" -> 5 * 5 // 5 questions
        "Heart" -> 7 * 5 // 7 questions
        "Throat" -> 4 * 5 // 4 questions
        "Third Eye" -> 6 * 5 // 6 questions
        "Crown" -> 4 * 5 // 4 questions
        else -> 1 // Prevent divide by zero errors
    }
}

// ✅ Function to Get Chakra Bubble Positions
fun getChakraOffset(chakraName: String): Offset {
    return when (chakraName) {
        "Crown" -> Offset(0f, -240f)
        "Third Eye" -> Offset(0f, -170f)
        "Throat" -> Offset(0f, -100f)
        "Heart" -> Offset(0f, -30f)
        "Solar Plexus" -> Offset(0f, 40f)
        "Sacral" -> Offset(0f, 110f)
        "Root" -> Offset(0f, 180f)
        else -> Offset.Zero
    }
}

// ✅ Function to Get Chakra Colors
fun getChakraColor(chakraName: String): Color {
    return when (chakraName) {
        "Crown" -> Color(0xFF6A0DAD)
        "Third Eye" -> Color(0xFF4B0082)
        "Throat" -> Color(0xFF0000FF)
        "Heart" -> Color(0xFF008000)
        "Solar Plexus" -> Color(0xFFFFFF00)
        "Sacral" -> Color(0xFFFFA500)
        "Root" -> Color(0xFFFF0000)
        else -> Color.Gray
    }
}

// ✅ Chakra Bubble with Animated Fill
@Composable
fun ChakraBubble(imbalancePercentage: Int, color: Color, offset: Offset) {
    val animation by animateFloatAsState(
        targetValue = (1 - (imbalancePercentage / 100f)), // 🔄 Inverting the percentage
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .size((60 * animation).dp) // 🛠️ Shrinking size based on imbalance percentage
            .offset(x = offset.x.dp, y = offset.y.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = size.minDimension / 2
            )

            drawCircle(
                color = color,
                radius = size.minDimension / 2 * animation
            )

            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 2,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // ✅ Show Imbalance Percentage Inside the Bubble
        Text(
            text = "$imbalancePercentage%",
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

