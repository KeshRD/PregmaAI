package com.kairaxus.bundymom
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

// Utils.kt
fun getTimeOfDayGreeting(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "morning"
        in 12..17 -> "afternoon"
        in 18..21 -> "evening"
        else -> "night"
    }
}

fun getUserName(): String {
    val email = FirebaseAuth.getInstance().currentUser?.email ?: return "User"
    return email.substringBefore("@").replaceFirstChar { it.uppercase() }
}

//data class ToolItem(val name: String, val iconRes: Int)