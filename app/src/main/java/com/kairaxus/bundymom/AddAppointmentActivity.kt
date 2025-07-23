package com.kairaxus.bundymom
import androidx.compose.ui.Alignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import java.text.SimpleDateFormat
import java.util.*

class AddAppointmentActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val selectedDate = intent.getStringExtra("selectedDate") ?: getCurrentDate()

        setContent {
            BundyMomTheme {
                AddAppointmentScreen(
                    auth = auth,
                    db = db,
                    selectedDate = selectedDate,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    selectedDate: String,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("Doctor") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var babyHeartRate by remember { mutableStateOf("") }
    var hasReminder by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Appointment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Profession", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = profession == "Doctor",
                        onClick = { profession = "Doctor" },
                        label = { Text("Doctor") }
                    )
                    FilterChip(
                        selected = profession == "Midwife",
                        onClick = { profession = "Midwife" },
                        label = { Text("Midwife") }
                    )
                    FilterChip(
                        selected = profession == "Other",
                        onClick = { profession = "Other" },
                        label = { Text("Other") }
                    )
                }

                Text("Date: $selectedDate", style = MaterialTheme.typography.bodyLarge)

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 15:30)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text("Health Metrics (optional)", style = MaterialTheme.typography.labelLarge)

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bloodPressure,
                    onValueChange = { bloodPressure = it },
                    label = { Text("Blood Pressure") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = babyHeartRate,
                    onValueChange = { babyHeartRate = it },
                    label = { Text("Baby's Heart Rate") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                    Text("Set Reminder?", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Button(
                onClick = {
                    val appointment = Appointment(
                        name = name,
                        profession = profession,
                        date = selectedDate,
                        time = time,
                        location = location,
                        notes = notes,
                        weight = weight,
                        bloodPressure = bloodPressure,
                        babyHeartRate = babyHeartRate,
                        hasReminder = hasReminder
                    )

                    auth.currentUser?.email?.let { email ->
                        db.collection("users").document(email)
                            .collection("appointments")
                            .add(appointment)
                            .addOnSuccessListener { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && time.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Appointment")
            }
        }
    }
}

fun getCurrentDate(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
}