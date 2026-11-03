package com.karen.citasdeportivas.network

import com.karen.citasdeportivas.model.Appointment
import com.karen.citasdeportivas.model.AuthResponse
import com.karen.citasdeportivas.model.LoginRequest
import com.karen.citasdeportivas.model.Professional
import com.karen.citasdeportivas.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ----- Autenticación -----
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // ----- Profesionales -----
    @GET("api/professionals")
    suspend fun getProfessionals(): Response<List<Professional>>

    @GET("api/professionals/{id}")
    suspend fun getProfessional(@Path("id") id: Long): Response<Professional>

    @POST("api/professionals")
    suspend fun createProfessional(@Body professional: Professional): Response<Professional>

    @PUT("api/professionals/{id}")
    suspend fun updateProfessional(
        @Path("id") id: Long,
        @Body professional: Professional
    ): Response<Professional>

    @DELETE("api/professionals/{id}")
    suspend fun deleteProfessional(@Path("id") id: Long): Response<Unit>

    // ----- Citas -----
    @GET("api/appointments")
    suspend fun getAppointments(
        @Query("professionalId") professionalId: Long? = null,
        @Query("date") date: String? = null,
        @Query("status") status: String? = null
    ): Response<List<Appointment>>

    @GET("api/appointments/{id}")
    suspend fun getAppointment(@Path("id") id: Long): Response<Appointment>

    @POST("api/appointments")
    suspend fun createAppointment(@Body appointment: Appointment): Response<Appointment>

    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") id: Long,
        @Body appointment: Appointment
    ): Response<Appointment>

    @DELETE("api/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Long): Response<Unit>

    @PATCH("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: Long): Response<Appointment>

    // Solo el admin puede marcar una cita como atendida
    @PATCH("api/appointments/{id}/attend")
    suspend fun attendAppointment(@Path("id") id: Long): Response<Appointment>
}