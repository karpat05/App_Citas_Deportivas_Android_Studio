package com.karen.citasdeportivas.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String // "ADMIN" o "USER"
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