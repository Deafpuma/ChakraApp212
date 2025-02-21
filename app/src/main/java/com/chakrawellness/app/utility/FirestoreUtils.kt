package com.chakrawellness.app.utility

import android.util.Log
import com.chakrawellness.app.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source

object FirestoreUtils {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ✅ Save User Profile
    fun saveUserProfile(
        userId: String,
        profile: UserProfile,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userId.isEmpty()) {
            onFailure("Invalid userId")
            return
        }

        firestore.collection("Users")
            .document(userId)
            .set(profile)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Unknown error")
            }
    }

    // ✅ Save Quiz Results
    fun saveQuizResults(
        userId: String,  // ✅ Ensure this parameter exists
        quizResults: Map<String, Map<String, Int>>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userId.isEmpty()) {
            onFailure("User is not authenticated")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId)
            .collection("QuizResults")
            .add(
                mapOf(
                    "quizResults" to quizResults,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Unknown error") }
    }



    // ✅ Check if User Profile Exists
    fun checkIfProfileExists(onResult: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("FirestoreUtils", "User not authenticated")
            onResult(false)
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("FirestoreUtils", "Profile found for user: $userId")
                    onResult(true)
                } else {
                    Log.d("FirestoreUtils", "No profile found for user: $userId")
                    onResult(false)
                }
            }
            .addOnFailureListener {
                Log.e("FirestoreUtils", "Error checking profile: ${it.message}")
                onResult(false)
            }
    }

    fun getLatestQuizResults(userId: String, callback: (Map<String, Map<String, Int>>) -> Unit) {
        firestore.collection("Users").document(userId).collection("QuizResults")
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



    // ✅ Fetch Quiz Results
    fun getQuizResults(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onFailure("User is not authenticated")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("Users").document(userId).collection("QuizResults")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val quizResults = documents.map { it.data }
                Log.d("FirestoreUtils", "Fetched Quiz Results: $quizResults") // ✅ Debugging
                onSuccess(quizResults)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUtils", "Error fetching quiz results: ${e.message}")
                onFailure(e.message ?: "Unknown error")
            }
    }

}
