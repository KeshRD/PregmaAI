package com.kairaxus.bundymom

import android.content.Intent
import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// NavItem now takes drawable resource ID instead of ImageVector
data class NavItem(
    val title: String,
    val route: String,
    @DrawableRes val iconRes: Int
)

@Composable
fun BottomNavigationBar(currentRoute: String) {
    val context = LocalContext.current
    val navItems = listOf(
        NavItem("AI Calendar Home", "today", R.drawable.calander_icon),
        NavItem("Baby Kick Counter", "discover", R.drawable.baby_icon),
        NavItem("Check Symptoms", "tools", R.drawable.symptoms_icon),
        NavItem("Chat AI with Mid Wife", "premium", R.drawable.ar_baby_icon)
    )

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier = Modifier.size(40.dp)
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    when (item.route) {
                        "today" -> {
                            if (context !is DashboardActivity) {
                                context.startActivity(Intent(context, DashboardActivity::class.java))
                                (context as? Activity)?.finish()
                            }
                        }
                        "discover" -> {
                            if (context !is DiscoverActivity) {
                                context.startActivity(Intent(context, GameKickCounterActivity::class.java))
                                (context as? Activity)?.finish()
                            }
                        }
                        "tools" -> {
                            if (context !is AIDoctorActivity) {
                                context.startActivity(Intent(context, AIDoctorActivity::class.java))
                                (context as? Activity)?.finish()
                            }
                        }
                        "premium" -> {
                            if (context !is PremiumActivity) {
                                context.startActivity(Intent(context, chatMainActivity::class.java))
                                (context as? Activity)?.finish()
                            }
                        }
                    }
                }
            )
        }
    }
}
