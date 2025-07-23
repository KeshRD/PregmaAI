package com.kairaxus.bundymom
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource



class DashboardActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundyMomTheme {
                DashboardScreenWithFirebase(auth, db)

            }
            toolbutton()
            aisummerybutton()
        }
    }
}


@Composable
fun toolbutton() {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.go_to_tools),
            contentDescription = "Go to Tools",
            modifier = Modifier
                .offset(x = 300.dp, y = 50.dp)
                .size(50.dp)
                .clickable {
                    context.startActivity(Intent(context, ToolsActivity::class.java))
                }
        )
    }

}

@Composable
fun aisummerybutton() {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.aisummaraypic),
            contentDescription = "Go to Tools",
            modifier = Modifier
                .offset(x = 250.dp, y = 50.dp)
                .size(50.dp)
                .clickable {
                    context.startActivity(Intent(context, ToolsActivity::class.java))
                }
        )
    }

}



@Composable
fun DashboardScreenWithFirebase(
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    val context = LocalContext.current
    var pregnancyData by remember { mutableStateOf<PregnancyData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fetchPregnancyData(auth, db,
            onSuccess = { data ->
                pregnancyData = data
                isLoading = false
            },
            onError = { message ->
                errorMessage = message
                isLoading = false
            }
        )
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
    } else if (errorMessage != null) {
        Text(text = errorMessage!!, modifier = Modifier.fillMaxSize().wrapContentSize())
    } else {
        pregnancyData?.let { data ->
            DashboardScreen(
                dayCount = data.daysPassed,
                weeksPregnant = data.weeksPassed,
                trimester = data.trimester,
                dueDate = data.formattedDueDate,
                progress = data.progress,
                onEditClick = {
                    // Handle edit click - navigate to DueDateActivity
                    context.startActivity(Intent(context, DueDateActivity::class.java))
                }
            )
        } ?: Text("No pregnancy data found", modifier = Modifier.fillMaxSize().wrapContentSize())
    }
}

@Composable
fun DashboardScreen(
    dayCount: Int,
    weeksPregnant: Int,
    trimester: String,
    dueDate: String,
    progress: Float,
    onEditClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(currentRoute = "today") }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Text(
                    text = "Good ${getTimeOfDayGreeting()}, ${getUserName()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Pregnancy Progress Card
                PregnancyProgressCard(
                    dayCount = dayCount,
                    weeksPregnant = weeksPregnant,
                    trimester = trimester,
                    dueDate = dueDate,
                    progress = progress,
                    onEditClick = onEditClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Appointments embedded below pregnancy info


                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                        .scale(0.8f), // Scales down everything inside
                    elevation = CardDefaults.cardElevation(5.dp)
                ) {
                    AppointmentsScreen(auth = auth, db = db)
                }
            }
        }
    }
}


@Composable
fun PregnancyProgressCard(
    dayCount: Int,
    weeksPregnant: Int,
    trimester: String,
    dueDate: String,
    progress: Float,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Day $dayCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* Handle expand */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Expand"
                    )
                }
            }

            Text(
                text = "$weeksPregnant weeks pregnant",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = trimester,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Due $dueDate",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onEditClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Due Date")
            }
        }
    }
}

data class PregnancyData(
    val daysPassed: Int,
    val weeksPassed: Int,
    val trimester: String,
    val formattedDueDate: String,
    val progress: Float
)

private suspend fun fetchPregnancyData(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onSuccess: (PregnancyData) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val userEmail = auth.currentUser?.email ?: run {
            onError("User not logged in")
            return
        }

        db.collection("users").document(userEmail)
            .collection("profile").document("dueDate")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.getString("dueDate")?.let { dueDateString ->
                        val pregnancyData = calculatePregnancyData(dueDateString)
                        onSuccess(pregnancyData)
                    } ?: onError("Due date not available")
                } else {
                    onError("Due date not set")
                }
            }
            .addOnFailureListener {
                onError("Error fetching data")
            }
    } catch (e: Exception) {
        onError("Error: ${e.message}")
    }
}

private fun calculatePregnancyData(dueDateString: String): PregnancyData {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dueDate = sdf.parse(dueDateString)
    val today = Calendar.getInstance()

    val diffInMillis = dueDate.time - today.timeInMillis
    val daysLeft = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
    val daysPassed = 280 - daysLeft // Total pregnancy days
    val weeksPassed = daysPassed / 7
    val totalWeeks = 40
    val progress = weeksPassed.toFloat() / totalWeeks

    val trimester = when {
        weeksPassed < 13 -> "First trimester"
        weeksPassed < 27 -> "Second trimester"
        else -> "Third trimester"
    }

    val dueDateDisplayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val formattedDueDate = dueDateDisplayFormat.format(dueDate)

    return PregnancyData(
        daysPassed = daysPassed,
        weeksPassed = weeksPassed,
        trimester = trimester,
        formattedDueDate = formattedDueDate,
        progress = progress
    )
}
