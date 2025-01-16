package com.chakrawellness.app.utility

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

object FirestoreUtils {

    fun saveUserData(
        email: String,
        name: String,
        profilePicture: String,
        age: Int,
        weight: Int,
        feet: Int,
        inches: Int
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        val userData = hashMapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "email" to email,
            "lastLogin" to FieldValue.serverTimestamp(),
            "name" to name,
            "profilePicture" to profilePicture,
            "age" to age,
            "weight" to weight,
            "height" to mapOf(
                "feet" to feet,
                "inches" to inches
            )
        )

        userId?.let {
            db.collection("Users").document(it)
                .set(userData)
                .addOnSuccessListener {
                    Log.d("Firestore", "Document successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error writing document", e)
                }
        }
    }
    fun fetchUserData(onSuccess: (Map<String, Any>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        userId?.let {
            db.collection("Users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        onSuccess(document.data ?: emptyMap())
                    } else {
                        Log.d("Firestore", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting document", e)
                    onFailure(e)
                }
        }
    }
    fun getUserData(userId: String, callback: (Map<String, Any>?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(document.data)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
