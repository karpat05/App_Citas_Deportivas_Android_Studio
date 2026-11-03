package com.karen.citasdeportivas

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/** Se ejecuta una sola vez al abrir la app; prepara la sesión antes de cualquier pantalla. */
class CitasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Diseño claro fijo, para que los colores se vean igual en todos los celulares
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        SessionManager.init(this)
    }
}