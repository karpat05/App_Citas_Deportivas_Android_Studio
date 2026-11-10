package com.karen.citasdeportivas.network

import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

const val CONNECTION_ERROR =
    "No se pudo conectar con el servidor. Verifica que la API esté encendida."
const val TIMEOUT_ERROR = "El servidor tardó demasiado en responder. Intenta de nuevo."
const val UNEXPECTED_ERROR = "Ocurrió un error inesperado. Intenta de nuevo."

/**
 * Lee el mensaje de error que manda la API, que siempre tiene la forma {"error": "..."}.
 * Sirve para mostrar en pantalla los 400, 404 y 409 (por ejemplo, el cruce de horarios).
 * Si la API no mandó mensaje, usa uno claro según el código HTTP.
 */
fun Response<*>.apiError(): String {
    val fromApi = try {
        JSONObject(errorBody()?.string().orEmpty()).optString("error")
    } catch (e: Exception) {
        ""
    }
    if (fromApi.isNotBlank()) return fromApi
    return when (code()) {
        401 -> "Tu sesión venció. Inicia sesión de nuevo."
        403 -> "No tienes permiso para hacer esto."
        404 -> "No se encontró lo que buscabas."
        in 500..599 -> "El servidor tuvo un problema. Intenta más tarde."
        else -> "Error ${code()}"
    }
}

/**
 * Mensaje para el usuario cuando una llamada a la API lanza una excepción:
 * sin conexión, tiempo agotado o cualquier otro fallo inesperado (así la app nunca se cierra).
 */
fun Throwable.userMessage(): String = when (this) {
    is SocketTimeoutException -> TIMEOUT_ERROR // va primero: es un tipo de IOException
    is IOException -> CONNECTION_ERROR
    else -> UNEXPECTED_ERROR
}