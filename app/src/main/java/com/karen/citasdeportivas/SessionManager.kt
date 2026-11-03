package com.karen.citasdeportivas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.karen.citasdeportivas.model.User

/** Guarda la sesión (token, nombre y rol) en el teléfono para no pedir login cada vez. */
object SessionManager {

    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
    }

    val token: String? get() = prefs.getString("token", null)
    val userName: String get() = prefs.getString("name", "") ?: ""
    val role: String get() = prefs.getString("role", "") ?: ""

    val isLoggedIn: Boolean get() = !token.isNullOrEmpty()
    val isAdmin: Boolean get() = role == "ADMIN"

    fun save(token: String, user: User) {
        prefs.edit()
            .putString("token", token)
            .putString("name", user.name)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()
    }

    /** Borra la sesión y regresa a la pantalla de login, cerrando todas las demás. */
    fun logout() {
        prefs.edit().clear().apply()
        val intent = Intent(appContext, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        appContext.startActivity(intent)
    }
}