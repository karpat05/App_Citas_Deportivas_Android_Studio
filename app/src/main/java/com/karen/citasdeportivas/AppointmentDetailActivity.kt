package com.karen.citasdeportivas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.karen.citasdeportivas.databinding.ActivityAppointmentDetailBinding
import com.karen.citasdeportivas.model.Appointment
import com.karen.citasdeportivas.network.CONNECTION_ERROR
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Detalle de una cita, con las acciones según el rol y el estado:
 * - Marcar como atendida: solo ADMIN y solo si está PENDIENTE
 * - Cancelar: ADMIN o el dueño de la cita, solo si está PENDIENTE
 * - Editar y eliminar: solo ADMIN
 */
class AppointmentDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "appointment_id"
    }

    private lateinit var binding: ActivityAppointmentDetailBinding
    private var appointmentId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appointmentId = intent.getLongExtra(EXTRA_ID, -1L)

        binding.btnRetry.setOnClickListener { load() }

        binding.btnAttend.setOnClickListener {
            confirm(
                "Marcar como atendida",
                "¿Confirmas que esta cita ya fue atendida?",
                "Marcar"
            ) { attend() }
        }
        binding.btnCancel.setOnClickListener {
            confirm(
                "Cancelar cita",
                "¿Seguro que quieres cancelar esta cita?",
                "Cancelar cita"
            ) { cancel() }
        }
        binding.btnEdit.setOnClickListener {
            startActivity(
                Intent(this, AppointmentFormActivity::class.java)
                    .putExtra(AppointmentFormActivity.EXTRA_ID, appointmentId)
            )
        }
        binding.btnDelete.setOnClickListener {
            confirm(
                "Eliminar cita",
                "¿Seguro que quieres eliminar esta cita? Esta acción no se puede deshacer.",
                "Eliminar"
            ) { delete() }
        }
    }

    // Se recarga al volver, para ver los cambios hechos en el formulario de edición
    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAppointment(appointmentId)
                val appointment = response.body()
                if (!response.isSuccessful || appointment == null) {
                    showError(response.apiError())
                    return@launch
                }
                // El nombre del profesional se consulta aparte; si falla, se muestra su id
                val professional = RetrofitClient.api.getProfessional(appointment.professionalId)
                val professionalName = professional.body()?.name
                    ?: "Profesional #${appointment.professionalId}"
                showAppointment(appointment, professionalName)
            } catch (e: Exception) {
                showError(CONNECTION_ERROR)
            }
        }
    }

    private fun showAppointment(appointment: Appointment, professionalName: String) {
        binding.tvAthlete.text = appointment.athleteName
        binding.tvProfessional.text = "Profesional: $professionalName"
        binding.tvWhen.text =
            "${appointment.date}  ·  ${appointment.startTime} - ${appointment.endTime}"
        binding.tvReason.text = "Motivo: ${appointment.reason}"
        binding.tvStatus.text = appointment.status
        binding.tvStatus.setTextColor(
            Color.parseColor(
                when (appointment.status) {
                    "ATENDIDA" -> "#1B6E3A"
                    "CANCELADA" -> "#B3261E"
                    else -> "#B26A00"
                }
            )
        )

        val pending = appointment.status == "PENDIENTE"
        val admin = SessionManager.isAdmin
        binding.btnAttend.visibility = if (admin && pending) View.VISIBLE else View.GONE
        binding.btnCancel.visibility = if (pending) View.VISIBLE else View.GONE
        binding.btnEdit.visibility = if (admin) View.VISIBLE else View.GONE
        binding.btnDelete.visibility = if (admin) View.VISIBLE else View.GONE

        binding.progressBar.visibility = View.GONE
        binding.layoutContent.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun attend() = runAction("Cita marcada como atendida") {
        RetrofitClient.api.attendAppointment(appointmentId)
    }

    private fun cancel() = runAction("Cita cancelada") {
        RetrofitClient.api.cancelAppointment(appointmentId)
    }

    // Ejecuta la acción; si sale bien, avisa y recarga el detalle para ver el nuevo estado
    private fun runAction(successMessage: String, call: suspend () -> Response<Appointment>) {
        lifecycleScope.launch {
            try {
                val response = call()
                if (response.isSuccessful) {
                    toast(successMessage)
                    load()
                } else {
                    toast(response.apiError())
                }
            } catch (e: Exception) {
                toast(CONNECTION_ERROR)
            }
        }
    }

    private fun delete() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.deleteAppointment(appointmentId)
                if (response.isSuccessful) {
                    toast("Cita eliminada")
                    finish()
                } else {
                    toast(response.apiError())
                }
            } catch (e: Exception) {
                toast(CONNECTION_ERROR)
            }
        }
    }

    private fun confirm(title: String, message: String, positive: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive) { _, _ -> onConfirm() }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvError.text = message
    }
}