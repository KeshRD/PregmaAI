package com.kairaxus.bundymom

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameEditText = findViewById<EditText>(R.id.edit_name)
        val ageEditText = findViewById<EditText>(R.id.edit_age)
        val pregnantSpinner = findViewById<Spinner>(R.id.spinner_pregnant)
        val childrenSpinner = findViewById<Spinner>(R.id.spinner_children)
        val continueButton = findViewById<Button>(R.id.btn_continue)

        val options = arrayOf("Yes", "No")
        pregnantSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        childrenSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        continueButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val age = ageEditText.text.toString()
            val pregnant = pregnantSpinner.selectedItem.toString()
            val hasChildren = childrenSpinner.selectedItem.toString()

            val userEmail = auth.currentUser?.email

            if (name.isEmpty() || age.isEmpty() || userEmail == null) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userInfo = hashMapOf(
                "name" to name,
                "age" to age,
                "pregnant" to pregnant,
                "children" to hasChildren
            )

            db.collection("users").document(userEmail).collection("profile")
                .document("info")
                .set(userInfo)
                .addOnSuccessListener {
                    startActivity(Intent(this, DueDateActivity::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
