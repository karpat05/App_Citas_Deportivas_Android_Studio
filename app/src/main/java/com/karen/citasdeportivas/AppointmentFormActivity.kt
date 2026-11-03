package com.karen.citasdeportivas

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.karen.citasdeportivas.databinding.ActivityAppointmentFormBinding
import com.karen.citasdeportivas.model.Appointment
import com.karen.citasdeportivas.model.Professional
import com.karen.citasdeportivas.network.CONNECTION_ERROR
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import com.karen.citasdeportivas.ui.setupDatePicker
import com.karen.citasdeportivas.ui.setupTimePicker
import kotlinx.coroutines.launch

/**
 * Sirve para agendar (POST) y para editar (PUT) una cita.
 * Si el horario se cruza con otra cita, la API responde 409 y el mensaje se muestra aquí.
 */
class AppointmentFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "appointment_id"
        const val EXTRA_PROFESSIONAL_ID = "professional_id"
    }

    private lateinit var binding: ActivityAppointmentFormBinding
    private val professionals = mutableListOf<Professional>()
    private val statuses = listOf("PENDIENTE", "ATENDIDA", "CANCELADA")
    private var editingId: Long = -1L
    private val isEditing: Boolean get() = editingId > 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getLongExtra(EXTRA_ID, -1L)
        val presetProfessional = intent.getLongExtra(EXTRA_PROFESSIONAL_ID, -1L)

        binding.tvFormTitle.text = if (isEditing) "Editar cita" else "Nueva cita"
        binding.layoutStatus.visibility = if (isEditing) View.VISIBLE else View.GONE

        // El usuario agenda a su propio nombre, así que no escribe el nombre del deportista
        if (!SessionManager.isAdmin) binding.etAthlete.visibility = View.GONE

        binding.spStatus.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statuses
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.etDate.setupDatePicker()
        binding.etStart.setupTimePicker()
        binding.etEnd.setupTimePicker()
        binding.btnSave.setOnClickListener { save() }

        loadData(presetProfessional)
    }

    private fun loadData(presetProfessional: Long) {
        setBusy(true)
        lifecycleScope.launch {
            try {
                val professionalsResponse = RetrofitClient.api.getProfessionals()
                if (!professionalsResponse.isSuccessful) {
                    showFormError(professionalsResponse.apiError())
                    setBusy(false)
                    return@launch
                }
                professionals.clear()
                professionals.addAll(professionalsResponse.body().orEmpty())

                binding.spProfessional.adapter = ArrayAdapter(
                    this@AppointmentFormActivity,
                    android.R.layout.simple_spinner_item,
                    professionals.map { it.name }
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                var selectedId: Long? = if (presetProfessional > 0) presetProfessional else null

                if (isEditing) {
                    val response = RetrofitClient.api.getAppointment(editingId)
                    val appointment = response.body()
                    if (response.isSuccessful && appointment != null) {
                        binding.etAthlete.setText(appointment.athleteName)
                        binding.etDate.setText(appointment.date)
                        binding.etStart.setText(appointment.startTime)
                        binding.etEnd.setText(appointment.endTime)
                        binding.etReason.setText(appointment.reason)
                        binding.spStatus.setSelection(
                            statuses.indexOf(appointment.status).coerceAtLeast(0)
                        )
                        selectedId = appointment.professionalId
                    } else {
                        showFormError(response.apiError())
                        setBusy(false)
                        return@launch
                    }
                }

                if (selectedId != null) {
                    val index = professionals.indexOfFirst { it.id == selectedId }
                    if (index >= 0) binding.spProfessional.setSelection(index)
                }
            } catch (e: Exception) {
                showFormError(CONNECTION_ERROR)
            }
            setBusy(false)
        }
    }

    private fun save() {
        val athlete = if (SessionManager.isAdmin) {
            binding.etAthlete.text.toString().trim()
        } else {
            SessionManager.userName
        }
        val date = binding.etDate.text.toString().trim()
        val start = binding.etStart.text.toString().trim()
        val end = binding.etEnd.text.toString().trim()
        val reason = binding.etReason.text.toString().trim()

        val position = binding.spProfessional.selectedItemPosition
        val professionalId = professionals.getOrNull(position)?.id
        if (professionalId == null) {
            showFormError("Selecciona un profesional (si no hay ninguno, crea uno primero)")
            return
        }
        if (athlete.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty() || reason.isEmpty()) {
            showFormError("Completa todos los campos")
            return
        }

        val status = if (isEditing) {
            statuses.getOrElse(binding.spStatus.selectedItemPosition) { "PENDIENTE" }
        } else {
            "PENDIENTE"
        }

        val appointment = Appointment(
            athleteName = athlete,
            professionalId = professionalId,
            date = date,
            startTime = start,
            endTime = end,
            status = status,
            reason = reason
        )

        setBusy(true)
        lifecycleScope.launch {
            try {
                val response = if (isEditing) {
                    RetrofitClient.api.updateAppointment(editingId, appointment)
                } else {
                    RetrofitClient.api.createAppointment(appointment)
                }
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AppointmentFormActivity,
                        if (isEditing) "Cita actualizada" else "Cita agendada",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    setBusy(false)
                    showFormError(response.apiError())
                }
            } catch (e: Exception) {
                setBusy(false)
                showFormError(CONNECTION_ERROR)
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !busy
        if (busy) binding.tvFormError.visibility = View.GONE
    }

    private fun showFormError(message: String) {
        binding.tvFormError.text = message
        binding.tvFormError.visibility = View.VISIBLE
    }
}