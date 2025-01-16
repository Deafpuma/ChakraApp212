package com.chakrawellness.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val welcomeText = findViewById<TextView>(R.id.welcome_text)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        // Display the user's email
        welcomeText.text = "Welcome, ${user?.email ?: "User"}!"

        // Set up logout button
        logoutButton.setOnClickListener {
            auth.signOut()
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}