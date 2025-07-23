// DiscoverActivity.kt
package com.kairaxus.bundymom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import androidx.compose.material3.*

class DiscoverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundyMomTheme {
                DiscoverScreen()
            }
        }
    }
}

@Composable
fun DiscoverScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(currentRoute = "discover") }
    ) { innerPadding ->
        Text(
            text = "Discover Screen Content",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverScreenPreview() {
    BundyMomTheme {
        DiscoverScreen()
    }
}