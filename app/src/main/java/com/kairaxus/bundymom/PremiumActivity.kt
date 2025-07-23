// PremiumActivity.kt
package com.kairaxus.bundymom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp

class PremiumActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundyMomTheme {
                PremiumScreen()
            }
        }
    }
}

@Composable
fun PremiumScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(currentRoute = "premium") }
    ) { innerPadding ->
        // Add scroll behavior
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Replace this with your real content
            for (i in 1..50) {
                Text(
                    text = "Premium Feature #$i",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PremiumScreenPreview() {
    BundyMomTheme {
        PremiumScreen()
    }
}