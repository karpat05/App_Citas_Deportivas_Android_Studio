package com.karen.citasdeportivas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.karen.citasdeportivas.databinding.ActivityAppointmentsBinding
import com.karen.citasdeportivas.model.Appointment
import com.karen.citasdeportivas.model.Professional
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import com.karen.citasdeportivas.network.userMessage
import com.karen.citasdeportivas.ui.AppointmentAdapter
import com.karen.citasdeportivas.ui.setupDatePicker
import kotlinx.coroutines.launch

/**
 * Lista de citas con filtros que viajan como parámetros a la API:
 * GET /api/appointments?professionalId=1&date=2026-10-15&status=PENDIENTE
 * Al tocar una cita se abre su pantalla de detalle.
 */
class AppointmentsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PROFESSIONAL_ID = "professional_id"
    }

    private lateinit var binding: ActivityAppointmentsBinding
    private val professionals = mutableListOf<Professional>()
    private val statuses = listOf("Todos", "PENDIENTE", "ATENDIDA", "CANCELADA")

    // Filtro de profesional que llega desde la pantalla de detalle (solo se usa una vez)
    private var pendingProfessionalFilter: Long? = null

    private val adapter = AppointmentAdapter(
        professionalName = { id ->
            professionals.firstOrNull { it.id == id }?.name ?: "Profesional #$id"
        },
        onClick = { appointment -> openDetail(appointment) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preset = intent.getLongExtra(EXTRA_PROFESSIONAL_ID, -1L)
        pendingProfessionalFilter = if (preset > 0) preset else null

        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter

        binding.spStatus.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statuses
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.etDate.setupDatePicker()

        // ADMIN ve todas las citas. USER ve solo las suyas ("Mis citas") y esta es su pantalla principal.
        binding.tvTitle.text = when {
            SessionManager.isAdmin -> "Citas"
            SessionManager.isProfessional -> "Mi agenda"
            else -> "Mis citas"
        }
        // La barra con "Profesionales" y "Cerrar sesión" se muestra para los dos roles
        binding.layoutUserBar.visibility = View.VISIBLE
        // El profesional solo ve su propia agenda: no agenda citas ni explora a otros profesionales
        if (SessionManager.isProfessional) {
            binding.btnProfessionals.visibility = View.GONE
            binding.spProfessional.visibility = View.GONE
            binding.btnNewAppointment.visibility = View.GONE
        }
        binding.btnProfessionals.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.btnLogout.setOnClickListener { confirmLogout() }

        binding.btnApply.setOnClickListener { load() }
        binding.btnClear.setOnClickListener {
            binding.spProfessional.setSelection(0)
            binding.spStatus.setSelection(0)
            binding.etDate.setText("")
            load()
        }
        binding.btnNewAppointment.setOnClickListener {
            startActivity(Intent(this, AppointmentFormActivity::class.java))
        }
        binding.btnRetry.setOnClickListener { load() }
    }

    // Se recarga al volver del detalle o del formulario, para ver los cambios
    override fun onResume() {
        super.onResume()
        load()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir?")
            .setPositiveButton("Salir") { _, _ -> SessionManager.logout() }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun openDetail(appointment: Appointment) {
        val id = appointment.id ?: return
        startActivity(
            Intent(this, AppointmentDetailActivity::class.java)
                .putExtra(AppointmentDetailActivity.EXTRA_ID, id)
        )
    }

    private fun selectedProfessionalId(): Long? {
        val position = binding.spProfessional.selectedItemPosition
        return if (position <= 0) null else professionals.getOrNull(position - 1)?.id
    }

    private fun fillProfessionalSpinner(keepId: Long?) {
        val names = listOf("Todos los profesionales") + professionals.map { it.name }
        binding.spProfessional.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            names
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val index = if (keepId == null) 0 else professionals.indexOfFirst { it.id == keepId } + 1
        binding.spProfessional.setSelection(index)
    }

    private fun load() {
        showLoading()
        lifecycleScope.launch {
            try {
                // 1. Profesionales: sirven para el filtro y para mostrar el nombre en cada cita
                val keepId = selectedProfessionalId() ?: pendingProfessionalFilter
                pendingProfessionalFilter = null
                val professionalsResponse = RetrofitClient.api.getProfessionals()
                if (!professionalsResponse.isSuccessful) {
                    showError(professionalsResponse.apiError())
                    return@launch
                }
                professionals.clear()
                professionals.addAll(professionalsResponse.body().orEmpty())
                fillProfessionalSpinner(keepId)

                // 2. Citas con los filtros elegidos
                val date = binding.etDate.text.toString().trim().ifEmpty { null }
                val statusPosition = binding.spStatus.selectedItemPosition
                val status = if (statusPosition > 0) statuses[statusPosition] else null

                val response = RetrofitClient.api.getAppointments(
                    professionalId = selectedProfessionalId(),
                    date = date,
                    status = status
                )
                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    adapter.submit(list)
                    showList(isEmpty = list.isEmpty())
                } else {
                    showError(response.apiError())
                }
            } catch (e: Exception) {
                showError(e.userMessage())
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvAppointments.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showList(isEmpty: Boolean) {
        binding.progressBar.visibility = View.GONE
        binding.rvAppointments.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvAppointments.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvError.text = message
    }
}