package com.chakrawellness.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.navigation.NavHostController
import com.chakrawellness.app.screens.CreateAccountScreen
import com.chakrawellness.app.screens.CreateProfileScreen
import com.chakrawellness.app.screens.DashboardScreen
import com.chakrawellness.app.screens.LoginScreen
import com.chakrawellness.app.R
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var navController: NavHostController

    companion object {
        private const val RC_SIGN_IN = 1001
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
            navController = rememberNavController()

            Scaffold {
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onCreateAccountClick = {
                                navController.navigate("createAccount")
                            },
                            onGoogleSignIn = {
                                val signInIntent = googleSignInClient.signInIntent
                                startActivityForResult(signInIntent, RC_SIGN_IN)
                            }
                        )
                    }

                    composable("dashboard") {
                        var isProfileCreated by remember { mutableStateOf(false) }

                        // Call the asynchronous function and update the state
                        checkIfProfileCreated { profileCreated ->
                            isProfileCreated = profileCreated
                        }

                        DashboardScreen(
                            onLogout = {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            },
                            onCreateProfileClick = {
                                navController.navigate("createProfile")
                            },
                            isProfileCreated = isProfileCreated
                        )
                    }

                    composable("createProfile") {
                        CreateProfileScreen(
                            onProfileSaved = {
                                navController.navigate("dashboard") {
                                    popUpTo("createProfile") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("createAccount") {
                        CreateAccountScreen(
                            onAccountCreated = {
                                navController.navigate("dashboard") {
                                    popUpTo("createAccount") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    handleGoogleSignIn(idToken)
                }
            } catch (e: ApiException) {
                Log.e("Google Sign-In", "Error: ${e.message}")
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignIn(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                runOnUiThread {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            } else {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                Log.e("Google Sign-In", "Error: ${task.exception}")
            }
        }
    }

    private fun checkIfProfileCreated(callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return callback(false)
        val userId = currentUser.uid

        val db = FirebaseFirestore.getInstance()
        db.collection("profiles").document(userId).get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error checking profile: ${exception.message}")
                callback(false)
            }
    }
    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                // Handle back action
            }
        } else {
            super.onBackPressed()
        }
    }

}