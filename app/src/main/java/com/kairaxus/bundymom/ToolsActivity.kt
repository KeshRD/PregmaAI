package com.kairaxus.bundymom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kairaxus.bundymom.ui.theme.BundyMomTheme

class ToolsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundyMomTheme {
                ToolsScreen()
            }
        }
    }
}

data class ToolItem(
    val title: String,
    val icon: ImageVector,
    val activityClass: Class<*>?
)

@Composable
fun ToolsScreen() {
    val context = LocalContext.current
    val tools = listOf(
        //ToolItem("Kick Counter", Icons.Default.Favorite, GameKickCounterActivity::class.java),
        ToolItem("Contractions", Icons.Default.Favorite, ContractionsActivity::class.java),
        //ToolItem("Appointments", Icons.Default.Favorite, AppointmentsActivity::class.java),
        ToolItem("Weight Tracker", Icons.Default.Favorite, WeightTrackerActivity::class.java),
        ToolItem("Blood Pressure", Icons.Default.Favorite, BloodPressureActivity::class.java),
        ToolItem("Scan Images", Icons.Default.Favorite, ScanImagesActivity::class.java),
        ToolItem("Symptoms", Icons.Default.Favorite, SymptomsTrackerActivity::class.java),
        ToolItem("Water Intake", Icons.Default.Favorite, WaterIntakeActivity::class.java)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        //bottomBar = { BottomNavigationBar(currentRoute = "tools") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "AI Pregnancy Tools",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tools) { tool ->
                    ToolButton(
                        title = tool.title,
                        icon = tool.icon,
                        onClick = {
                            if (tool.activityClass != null) {
                                context.startActivity(Intent(context, tool.activityClass))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Placeholder activities - you'll need to create these
class WeightTrackerActivity : ComponentActivity()
class BloodPressureActivity : ComponentActivity()
class ScanImagesActivity : ComponentActivity()
class SymptomsTrackerActivity : ComponentActivity()
class WaterIntakeActivity : ComponentActivity()

@Preview(showBackground = true)
@Composable
fun ToolsScreenPreview() {
    BundyMomTheme {
        ToolsScreen()
    }
}