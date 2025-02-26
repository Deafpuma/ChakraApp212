package com.chakrawellness.app.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.chakrawellness.app.models.UserProfile
import com.chakrawellness.app.models.QuizResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object FirestoreUtils {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun getUserProfile(
        userId: String,
        onSuccess: (UserProfile) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userId.isEmpty()) {
            onFailure("Invalid user ID")
            return
        }

        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    if (profile != null) {
                        onSuccess(profile)
                    } else {
                        onFailure("Failed to parse user profile")
                    }
                } else {
                    onFailure("Profile not found")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Unknown error")
            }
    }

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

        FirebaseFirestore.getInstance().collection("Users")
            .document(userId)
            .set(profile)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Unknown error")
            }
    }


    // ✅ Save Quiz Results
    fun saveQuizResults(
        userId: String,
        newQuizResults: Map<String, Map<String, Int>>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userId.isEmpty()) {
            onFailure("User is not authenticated")
            return
        }

        val userRef = firestore.collection("QuizResults").document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            // Get existing quiz results if they exist, otherwise use an empty map
            val existingResults = snapshot.get("quizResults") as? Map<String, Any> ?: emptyMap()

            // Convert existing results to mutable map
            val updatedResults = existingResults.toMutableMap()

            // Merge new results with existing results
            newQuizResults.forEach { (chakra, scores) ->
                val existingScores = (updatedResults[chakra] as? Map<String, Int>) ?: emptyMap()
                val mergedScores = existingScores + scores // Merge maps
                updatedResults[chakra] = mergedScores
            }

            // ✅ Debugging logs before saving
            Log.d("FirestoreUtils", "🔥 [MERGED RESULTS BEFORE SAVE] $updatedResults")

            // Update Firestore document with the merged results
            transaction.set(userRef, mapOf("userId" to userId, "quizResults" to updatedResults, "timestamp" to FieldValue.serverTimestamp()))
        }.addOnSuccessListener {
            Log.d("FirestoreUtils", "✅ [AFTER SAVE] Successfully saved merged quiz results.")
            onSuccess()
        }.addOnFailureListener { e ->
            Log.e("FirestoreUtils", "❌ Error saving quiz: ${e.message}")
            onFailure("❌ Error saving quiz: ${e.message}")
        }
    }

    // ✅ Fetch the Last Quiz Results
    fun getLatestQuizResults(
        userId: String,
        callback: (Map<String, Map<String, Int>>) -> Unit
    ) {
        if (userId.isEmpty()) {
            callback(emptyMap())
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            firestore.collection("QuizResults")
                .whereEqualTo("userId", userId) // ✅ Fetch quizzes by user ID
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    val document = documents.firstOrNull()
                    if (document == null) {
                        Log.e("FirestoreUtils", "❌ No quiz results found.")
                        callback(emptyMap())
                        return@addOnSuccessListener
                    }

                    val rawResults = document.get("quizResults") as? Map<String, Any> ?: emptyMap()

                    // ✅ Debugging log before conversion
                    Log.d("FirestoreUtils", "🔥 [RAW DATA] Retrieved quizResults: $rawResults")

                    // ✅ Convert values to the correct type
                    val formattedResults = rawResults.mapValues { (_, scores) ->
                        (scores as? Map<*, *>)?.mapKeys { it.key.toString() }
                            ?.mapValues { (_, value) -> (value as? Number)?.toInt() ?: 0 }
                            ?: emptyMap()
                    }

                    Log.d("FirestoreUtils", "✅ [FORMATTED] Fetched quiz results: $formattedResults")
                    callback(formattedResults)
                }
                .addOnFailureListener {
                    Log.e("FirestoreUtils", "❌ Error fetching quiz results: ${it.message}")
                    callback(emptyMap()) // Return empty on failure
                }
        }
    }

    // ✅ Ensure Total Light Duration is Always 21 Minutes
    fun calculateLightDurations(quizResults: Map<String, Map<String, Int>>): Map<String, Double> {
        val chakraScores = mutableMapOf<String, Double>()
        val minTime = 1.0
        val maxTime = 5.0

        // ✅ Step 1: Sum up each chakra’s imbalance score
        quizResults.forEach { (chakra, scores) ->
            val totalScore = scores.values.sum().toDouble()
            chakraScores[chakra] = totalScore
        }

        // ✅ Step 2: Normalize the scores so they fit within 21 minutes
        val totalScore = chakraScores.values.sum()
        return if (totalScore > 0) {
            chakraScores.mapValues { (_, score) ->
                val scaledTime = (score / totalScore) * 21
                scaledTime.coerceIn(minTime, maxTime) // ✅ Ensure each chakra has at least 1 min and at most 5 min
            }
        } else {
            chakraScores.mapValues { minTime } // Default to 1 min if no imbalances exist
        }
    }


    fun getLightDurations(userId: String, callback: (Map<String, Double>) -> Unit) {
        FirebaseFirestore.getInstance().collection("QuizResults").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val durations = document.get("lightDurations") as? Map<String, Double> ?: emptyMap()
                    callback(durations)
                } else {
                    callback(emptyMap())
                }
            }
            .addOnFailureListener { e ->
                callback(emptyMap())
            }
    }



    // ✅ Save Quiz Progress (Auto-Save for Each Question)
    fun saveProgress(
        userId: String,
        chakraName: String,
        answers: Map<String, Int>
    ) {
        if (userId.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            firestore.collection("Users").document(userId)
                .collection("QuizProgress")
                .document(chakraName)
                .set(answers)
                .addOnSuccessListener {
                    Log.d("FirestoreUtils", "✅ Progress saved successfully for $chakraName")
                }
                .addOnFailureListener { error ->
                    Log.e("FirestoreUtils", "❌ Error saving progress: ${error.message}")
                }
        }
    }


    // ✅ Retrieve Quiz Progress (For Resume Feature)
    fun getSavedProgress(
        userId: String,
        chakraName: String,
        callback: (Map<String, Int>) -> Unit
    ) {
        if (userId.isEmpty()) {
            callback(emptyMap())
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            firestore.collection("Users").document(userId)
                .collection("QuizProgress")
                .document(chakraName)
                .get()
                .addOnSuccessListener { document ->
                    val savedData = document.data as? Map<String, Long> ?: emptyMap()
                    callback(savedData.mapValues { it.value.toInt() })
                }
                .addOnFailureListener {
                    callback(emptyMap()) // Return empty on error
                }
        }
    }
    // ✅ Fetch All Past Quiz Results
    fun getQuizResults(
        userId: String,
        onSuccess: (List<QuizResult>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userId.isEmpty()) {
            onFailure("Invalid user ID")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            firestore.collection("QuizResults")
                .whereEqualTo("userId", userId) // ✅ Fetch quizzes by user ID
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val results = documents.mapNotNull { doc ->
                        val rawResults = doc.get("quizResults") as? Map<String, Any> ?: emptyMap()

                        val formattedResults = rawResults.mapValues { (_, scores) ->
                            (scores as? Map<*, *>)?.mapKeys { it.key.toString() }
                                ?.mapValues { (_, value) -> (value as? Number)?.toInt() ?: 0 }
                                ?: emptyMap()
                        }

                        QuizResult(
                            quizResults = formattedResults,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    onSuccess(results)
                }
                .addOnFailureListener {
                    onFailure(it.message ?: "Unknown error")
                }
        }
    }



}
