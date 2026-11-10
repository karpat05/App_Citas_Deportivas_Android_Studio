package com.karen.citasdeportivas.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String, // "ADMIN", "USER" o "PROFESSIONAL"
    val professionalId: Long? = null // solo viene con valor si el rol es PROFESSIONAL
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: User
)