package com.kairaxus.bundymom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkUserAndRedirect()
    }

    private fun checkUserAndRedirect() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // No user logged in, go to login/signup
            startActivity(Intent(this, MainActivity ::class.java))
        } else {
            // Check if due date is set
            db.collection("users").document(currentUser.email!!)
                .collection("profile").document("dueDate")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Due date exists, go to dashboard
                        val intent = Intent(this, DashboardActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    } else {
                        // No due date set, go to due date activity
                        val intent = Intent(this, DueDateActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                    finish()
                }
                .addOnFailureListener {
                    // Error occurred, go to due date activity
                    val intent = Intent(this, DueDateActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
        }
    }
}