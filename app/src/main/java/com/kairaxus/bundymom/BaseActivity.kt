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
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

open class BaseActivity : AppCompatActivity() {
    protected lateinit var bottomNav: BottomNavigationView

    protected fun setupBottomNavigation(selectedItemId: Int = R.id.nav_today) {
        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.selectedItemId = selectedItemId
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> {
                    if (this !is DashboardActivity) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_discover -> {
                    if (this !is DiscoverActivity) {
                        startActivity(Intent(this, DiscoverActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_tools -> {
                    if (this !is ToolsActivity) {
                        startActivity(Intent(this, ToolsActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_premium -> {
                    if (this !is PremiumActivity) {
                        startActivity(Intent(this, PremiumActivity::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}