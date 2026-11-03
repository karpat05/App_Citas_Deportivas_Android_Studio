package com.karen.citasdeportivas.model

data class Appointment(
    val id: Long? = null,
    val athleteName: String,
    val professionalId: Long,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String = "PENDIENTE",
    val reason: String
)