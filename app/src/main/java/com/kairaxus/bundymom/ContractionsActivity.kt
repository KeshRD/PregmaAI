package com.kairaxus.bundymom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class ContractionsActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundyMomTheme {
                ContractionsScreen()
            }
        }
    }
}

@Composable
fun ContractionsScreen() {
    var isTracking by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf("00:00:00") }
    var contractionsList by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(isTracking) {
        while (isTracking) {
            delay(1000)
            val currentTime = System.currentTimeMillis()
            val diff = currentTime - startTime
            val seconds = (diff / 1000) % 60
            val minutes = (diff / (1000 * 60)) % 60
            val hours = (diff / (1000 * 60 * 60)) % 24
            elapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    Scaffold(
        topBar = {
            //CenterAlignedTopAppBar(
                //title = { Text("Contractions Timer") }
            //)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = elapsedTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    if (isTracking) {
                        // Stop tracking
                        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        contractionsList = contractionsList + listOf(elapsedTime)
                    } else {
                        // Start tracking
                        startTime = System.currentTimeMillis()
                    }
                    isTracking = !isTracking
                },
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.ThumbUp else Icons.Default.PlayArrow,
                    contentDescription = if (isTracking) "Stop" else "Start",
                    modifier = Modifier.size(48.dp)
                )
            }

            if (contractionsList.isNotEmpty()) {
                Text(
                    text = "Previous Contractions:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Column {
                    contractionsList.forEach { contraction ->
                        Text(text = contraction)
                    }
                }
            }
        }
    }
}