package com.karen.citasdeportivas.network

import org.json.JSONObject
import retrofit2.Response

const val CONNECTION_ERROR =
    "No se pudo conectar con el servidor. Verifica que la API esté encendida."

/**
 * Lee el mensaje de error que manda la API, que siempre tiene la forma {"error": "..."}.
 * Sirve para mostrar en pantalla los 400, 404 y 409 (por ejemplo, el cruce de horarios).
 */
fun Response<*>.apiError(): String {
    return try {
        val raw = errorBody()?.string().orEmpty()
        val message = JSONObject(raw).optString("error")
        if (message.isBlank()) "Error ${code()}" else message
    } catch (e: Exception) {
        "Error ${code()}"
    }
}