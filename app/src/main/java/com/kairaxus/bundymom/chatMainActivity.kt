package com.kairaxus.bundymom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.kairaxus.bundymom.ui.theme.BundyMomTheme


class chatMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setContent {
            BundyMomTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatPage(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = chatViewModel,
                        onBackClick = {
                            // Navigate to DashboardActivity instead of finishing
                            startActivity(Intent(this@chatMainActivity, DashboardActivity::class.java))
                            finish() // Optional: removes current activity from stack
                        }
                    )
                }
            }
        }
    }
}