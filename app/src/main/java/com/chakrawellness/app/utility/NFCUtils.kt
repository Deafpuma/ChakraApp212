package com.chakrawellness.app.utility

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

// ✅ Fetch Light Durations from Firestore when NFC is scanned
fun onNfcTagDetected(userId: String, context: Context) {
    getLightDurations(userId) { lightDurations ->
        if (lightDurations.isNotEmpty()) {
            sendLightDataToENTTEC(lightDurations, context)
        }
    }
}

// ✅ Retrieve Light Durations for User
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
            Log.e("NFCUtils", "❌ Error retrieving light durations: ${e.message}")
            callback(emptyMap())
        }
}

// ✅ Send Light Durations to ENTTEC API
fun sendLightDataToENTTEC(lightDurations: Map<String, Double>, context: Context) {
    val url = "http://ENTTEC_IP/api/control"  // Replace with actual ENTTEC API URL

    val requestBody = JSONObject().apply {
        put("userId", FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
        put("lightDurations", JSONObject(lightDurations.mapValues { it.value.toInt() })) // Convert to whole numbers
    }

    val request = JsonObjectRequest(
        Request.Method.POST, url, requestBody,
        { response ->
            Log.d("ENTTEC_API", "✅ Light settings sent successfully: $response")
        },
        { error ->
            Log.e("ENTTEC_API", "❌ Error sending light data: ${error.message}")
        }
    )

    Volley.newRequestQueue(context).add(request)
}
