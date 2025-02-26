package com.chakrawellness.app

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.chakrawellness.app.utility.FirestoreUtils
import com.chakrawellness.app.models.UserProfile
import com.chakrawellness.app.utility.onNfcTagDetected
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        val currentUser = auth.currentUser

        setContent {
            val navController = rememberNavController()
            var userProfile by remember { mutableStateOf<UserProfile?>(null) }

            // ✅ Fetch user profile on login
            LaunchedEffect(currentUser?.uid) {
                currentUser?.uid?.let { userId ->
                    FirestoreUtils.getUserProfile(
                        userId,
                        onSuccess = { profile ->
                            userProfile = profile
                            Log.d("MainActivity", "User profile loaded: $profile")
                        },
                        onFailure = { error ->
                            Log.e("MainActivity", "Error loading user profile: $error")
                        }
                    )
                }
            }

            val startDestination = if (currentUser != null) "home" else "login"
            AppNavigation(navController, startDestination)
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
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    runOnUiThread {
                        setContent {
                            val navController = rememberNavController()
                            AppNavigation(navController, "home")
                        }
                    }
                } else {
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            if (userId.isNotEmpty()) {
                onNfcTagDetected(userId, this)
            }
        }
    }

}
