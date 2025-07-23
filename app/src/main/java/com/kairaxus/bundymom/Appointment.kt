package com.kairaxus.bundymom

data class Appointment(
    val id: String = "",
    val name: String = "",
    val profession: String = "Doctor",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val notes: String = "",
    val weight: String = "",
    val bloodPressure: String = "",
    val babyHeartRate: String = "",
    val hasReminder: Boolean = false
) {
    constructor() : this("", "", "Doctor", "", "", "", "", "", "", "", false)
}