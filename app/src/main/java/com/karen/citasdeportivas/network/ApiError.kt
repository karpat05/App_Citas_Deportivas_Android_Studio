package com.karen.citasdeportivas.network

import org.json.JSONObject
import retrofit2.Response

fun Response<*>.errorMessage(): String {
    return try {
        val body = errorBody()?.string().orEmpty()
        JSONObject(body).getString("error")
    } catch (e: Exception) {
        "Error del servidor (código ${code()})"
    }
}