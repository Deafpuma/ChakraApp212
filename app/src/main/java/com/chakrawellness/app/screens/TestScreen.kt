package com.chakrawellness.app.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun TestScreen(
    onComplete: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Take Test", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { onComplete(85) }) { // Simulate test completion
            Text("Submit Test")
        }
    }
}
fun saveTestScore(score: Int) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    db.collection("users").document(userId ?: "").collection("tests").add(
        mapOf(
            "score" to score,
            "timestamp" to System.currentTimeMillis()
        )
    ).addOnSuccessListener {
        Log.d("Firestore", "Test score saved")
    }.addOnFailureListener { e ->
        Log.e("Firestore", "Error saving test score", e)
    }
}
