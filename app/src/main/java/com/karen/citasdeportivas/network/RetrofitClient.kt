package com.karen.citasdeportivas.network

import com.karen.citasdeportivas.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 = el "localhost" de el PC visto desde el emulador.
    // El puerto 3000 es el de tu API de Node, y todas sus rutas empiezan con /api/
    private const val BASE_URL = "http://192.168.10.14:3000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Agrega el token a cada petición: Authorization: Bearer <token>
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val token = SessionManager.token
        val builder = request.newBuilder()
        if (!token.isNullOrEmpty()) builder.addHeader("Authorization", "Bearer $token")

        val response = chain.proceed(builder.build())

        // Si el token venció o ya no sirve (401), se cierra la sesión.
        // Las rutas /auth/ se excluyen: un 401 en el login solo significa contraseña incorrecta.
        if (response.code == 401 && !token.isNullOrEmpty() &&
            !request.url.encodedPath.contains("/auth/")
        ) {
            SessionManager.logout()
        }
        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}