package com.kairaxus.bundymom

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DueDateActivity : AppCompatActivity() {

    private lateinit var dateText: TextView
    private lateinit var selectedMethod: String
    private var selectedDate: Calendar? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_due_date)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val methodSpinner = findViewById<Spinner>(R.id.spinner_method)
        val pickDateButton = findViewById<Button>(R.id.btn_pick_date)
        val continueButton = findViewById<Button>(R.id.btn_continue)
        dateText = findViewById(R.id.text_selected_date)

        val methods = arrayOf("Select", "First day of last period", "Estimated due date", "Date of conception")
        methodSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, methods)

        methodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedMethod = methods[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        pickDateButton.setOnClickListener {
            val now = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateText.text = format.format(selectedDate!!.time)
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        continueButton.setOnClickListener {
            if (selectedDate == null || selectedMethod == "Select") {
                Toast.makeText(this, "Select method and date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dueDate: Date? = when (selectedMethod) {
                "First day of last period" -> {
                    val calendar = selectedDate!!.clone() as Calendar
                    calendar.add(Calendar.DAY_OF_YEAR, 280)  // Add 40 weeks
                    calendar.time  // Return Date
                }
                "Estimated due date" -> selectedDate!!.time
                "Date of conception" -> {
                    val calendar = selectedDate!!.clone() as Calendar
                    calendar.add(Calendar.DAY_OF_YEAR, 266)  // Add 38 weeks
                    calendar.time  // Return Date
                }
                else -> null
            }

            if (dueDate == null) return@setOnClickListener

            val dueDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDate)
            val userEmail = auth.currentUser?.email ?: return@setOnClickListener

            db.collection("users").document(userEmail)
                .collection("profile").document("dueDate")
                .set(mapOf("dueDate" to dueDateString))
                // In DueDateActivity.kt, modify the success listener:
                .addOnSuccessListener {
                    val intent = Intent(this, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("dueDate", dueDateString)
                    }
                    startActivity(intent)
                    finish() // Finish current activity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save due date", Toast.LENGTH_SHORT).show()
                }
        }
    }
}