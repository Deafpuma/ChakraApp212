package com.chakrawellness.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chakrawellness.app.screens.*
import com.chakrawellness.app.utility.FirestoreUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        setContent {
            val navController = rememberNavController()
            AppNavigation(navController)
        }
    }

    @Composable
    private fun AppNavigation(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    onGoogleSignInClick = { startGoogleSignIn() }, // ✅ Pass Google Sign-In
                    onCreateAccountClick = { navController.navigate("createAccount") } // ✅ Fix missing parameter
                )
            }


            composable("dashboard") {
                val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                var isProfileCreated by remember { mutableStateOf(false) }

                LaunchedEffect(userId) {
                    if (userId.isNotEmpty()) {
                        FirestoreUtils.checkIfProfileExists { exists ->
                            isProfileCreated = exists
                        }
                    }
                }


                DashboardScreen(
                    navController = navController,
                    isProfileCreated = isProfileCreated,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }

            composable("profile") {
                val userId = auth.currentUser?.uid.orEmpty()
                ProfileScreen(
                    navController = navController,
                    userId = userId
                )
            }

            composable("createProfile") {
                val userId = auth.currentUser?.uid.orEmpty()
                if (userId.isNotEmpty()) {
                    CreateProfileScreen(navController = navController, userId = userId)
                } else {
                    Log.e("Navigation", "Invalid userId: $userId")
                    Toast.makeText(
                        this@MainActivity,
                        "Error: Invalid user session.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            composable("quiz") {
                QuizScreen(navController = navController, onComplete = {
                    navController.popBackStack()
                })
            }
            composable("quizHistory") {  // ✅ Fix: Added missing route
                QuizHistoryScreen(navController = navController)
            }
            composable("quizResults") {
                val quizResults = remember { mutableStateOf<Map<String, Map<String, Int>>>(emptyMap()) }
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid.orEmpty()  // ✅ Ensure userId is retrieved inside the scope

                // Fetch quiz results from Firestore
                LaunchedEffect(Unit) {
                    if (userId.isNotEmpty()) {
                        FirestoreUtils.getLatestQuizResults(userId) { results ->
                            quizResults.value = results
                        }
                    }
                }

                QuizResultsScreen(navController, quizResults.value)
            }




            composable("createAccount") {
                CreateAccountScreen(
                    navController = navController,
                    onAccountCreated = {
                        val userId = auth.currentUser?.uid.orEmpty()
                        if (userId.isNotEmpty()) {
                            navController.navigate("createProfile")
                        }
                    }
                )
            }
        }
    }

    // ✅ Initiates Google Sign-In Process
    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // ✅ Handles Google Sign-In Results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    setContent {
                        val navController =
                            rememberNavController()  // ✅ Correct way to get NavController
                        handleGoogleSignIn(idToken, navController)
                    }
                }
            } catch (e: ApiException) {
                Log.e("Google Sign-In", "Error: ${e.message}")
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ✅ Handles Firebase Authentication with Google Sign-In
    private fun handleGoogleSignIn(idToken: String, navController: NavHostController) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid.orEmpty()
                    if (userId.isNotEmpty()) {
                        FirestoreUtils.checkIfProfileExists { exists ->
                            if (exists) {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                navController.navigate("createProfile") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        Log.e("Google Sign-In", "Invalid user ID")
                    }
                } else {
                    Log.e("Google Sign-In", "Failed: ${task.exception?.localizedMessage}")
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


    fun getLatestQuizResults(userId: String, callback: (Map<String, Map<String, Int>>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId).collection("QuizResults")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                val latestResult = documents.firstOrNull()?.data as? Map<String, Map<String, Int>> ?: emptyMap()
                callback(latestResult)
            }
            .addOnFailureListener {
                callback(emptyMap())
            }
    }
}


