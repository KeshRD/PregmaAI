package com.kairaxus.bundymom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundyMomTheme {
                AppointmentsScreen(auth, db)
            }
        }
    }
}

@Composable
fun MonthCalendarView(
    selectedDate: String,
    appointments: List<Appointment>,
    onDateSelected: (String) -> Unit,
    onMonthChanged: (Int) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        time = dateFormat.parse(selectedDate) ?: Date()
    }
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val appointmentDates = appointments.map { it.date }.toSet()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                if (currentMonth == 0) {
                    currentMonth = 11
                    currentYear--
                } else {
                    currentMonth--
                }
                onMonthChanged(currentMonth)
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous month")
            }

            Text(
                text = monthFormat.format(Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth)
                }.time),
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = {
                if (currentMonth == 11) {
                    currentMonth = 0
                    currentYear++
                } else {
                    currentMonth++
                }
                onMonthChanged(currentMonth)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Next month",
                    modifier = Modifier.rotate(180f)
                )
            }
        }

        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Days grid
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday

        var dayCounter = 1
        repeat(6) { week -> // Max 6 weeks in a month view
            if (dayCounter <= maxDays) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { dayOfWeek ->
                        if ((week == 0 && dayOfWeek >= firstDayOfWeek) || (week > 0 && dayCounter <= maxDays)) {
                            val thisDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, currentYear)
                                set(Calendar.MONTH, currentMonth)
                                set(Calendar.DAY_OF_MONTH, dayCounter)
                            }.time
                            val dateStr = dateFormat.format(thisDate)
                            val isSelected = dateStr == selectedDate
                            val hasAppointment = appointmentDates.contains(dateStr)

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onDateSelected(dateStr) }
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayCounter.toString(),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasAppointment) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.secondary,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                            dayCounter++
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onDelete: () -> Unit = {},
    onDetailsClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailsClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = appointment.profession,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete appointment")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = appointment.date,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = appointment.time,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (appointment.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(appointment.location, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (appointment.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: ${appointment.notes}", style = MaterialTheme.typography.bodySmall)
            }

            if (appointment.weight.isNotEmpty() || appointment.bloodPressure.isNotEmpty() || appointment.babyHeartRate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (appointment.weight.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Weight", style = MaterialTheme.typography.labelSmall)
                            Text(appointment.weight, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (appointment.bloodPressure.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BP", style = MaterialTheme.typography.labelSmall)
                            Text(appointment.bloodPressure, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (appointment.babyHeartRate.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Heart Rate", style = MaterialTheme.typography.labelSmall)
                            Text(appointment.babyHeartRate, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var selectedAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }

    // Fetch appointments for the current month
    LaunchedEffect(currentMonth) {
        auth.currentUser?.email?.let { email ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val firstDay = calendar.time
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val lastDay = calendar.time

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            db.collection("users").document(email)
                .collection("appointments")
                .whereGreaterThanOrEqualTo("date", dateFormat.format(firstDay))
                .whereLessThanOrEqualTo("date", dateFormat.format(lastDay))
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let {
                        appointments = it.documents.mapNotNull { doc ->
                            doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                        }
                        selectedAppointments = appointments.filter { it.date == selectedDate }
                    }
                }
        }
    }

    // Update selected appointments when date changes
    LaunchedEffect(selectedDate, appointments) {
        selectedAppointments = appointments.filter { it.date == selectedDate }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appointments") },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(
                            Intent(context, AddAppointmentActivity::class.java)
                                .putExtra("selectedDate", selectedDate)
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Appointment")
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
            MonthCalendarView(
                selectedDate = selectedDate,
                appointments = appointments,
                onDateSelected = { date -> selectedDate = date },
                onMonthChanged = { month -> currentMonth = month }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedAppointments.isEmpty()) {
                Text(
                    text = "No appointments for $selectedDate",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedAppointments) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onDelete = {
                                auth.currentUser?.email?.let { email ->
                                    db.collection("users").document(email)
                                        .collection("appointments")
                                        .document(appointment.id)
                                        .delete()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

//fun getCurrentDate(): String {
  //  return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
//}