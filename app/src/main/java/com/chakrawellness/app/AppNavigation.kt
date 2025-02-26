package com.chakrawellness.app

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chakrawellness.app.screens.*
import com.chakrawellness.app.utility.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String = "login") {
    val auth = FirebaseAuth.getInstance()

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(navController) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("home") {
            HomeScreen(navController)
        }

        // ✅ Ensure Quiz Order (Root to Crown)
        composable("quizRoot") { QuizScreen(navController, "Root") }
        composable("quizSacral") { QuizScreen(navController, "Sacral") }
        composable("quizSolarPlexus") { QuizScreen(navController, "Solar Plexus") }
        composable("quizHeart") { QuizScreen(navController, "Heart") }
        composable("quizThroat") { QuizScreen(navController, "Throat") }
        composable("quizThirdEye") { QuizScreen(navController, "Third Eye") }
        composable("quizCrown") { QuizScreen(navController, "Crown") }

        // ✅ Results Page Now Fetches Firebase Data Correctly
        composable("chakraResults") {
            val quizResults = remember { mutableStateOf<Map<String, Map<String, Int>>>(emptyMap()) }
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid.orEmpty()

            LaunchedEffect(Unit) {
                if (userId.isNotEmpty()) {
                    FirestoreUtils.getLatestQuizResults(userId) { results ->
                        quizResults.value = results
                    }
                }
            }

            ChakraResultsScreen(navController = navController)

        }


        composable("quizHistory") {
            QuizHistoryScreen(navController)
        }
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(navController, userId)
        }


        composable("quizResults") {
            val quizResults = remember { mutableStateOf<Map<String, Map<String, Int>>>(emptyMap()) }
            val userId = auth.currentUser?.uid.orEmpty()

            LaunchedEffect(Unit) {
                if (userId.isNotEmpty()) {
                    FirestoreUtils.getLatestQuizResults(userId) { results ->
                        quizResults.value = results
                    }
                }
            }

            QuizResultsScreen(navController, quizResults.value)
        }

        composable("createProfile") {
            CreateProfileScreen(navController, userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
        }

    }
}
