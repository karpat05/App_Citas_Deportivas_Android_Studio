package com.karen.citasdeportivas

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.karen.citasdeportivas.databinding.ActivityProfessionalAgendaBinding
import com.karen.citasdeportivas.model.Appointment
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import com.karen.citasdeportivas.network.userMessage
import com.karen.citasdeportivas.ui.AgendaAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Agenda del profesional (HU-16 y HU-17).
 * Abre en las citas de HOY ordenadas por hora. Con las flechas o el selector de fecha
 * se cambia de día para planificar la semana.
 * Llama a GET /api/appointments?professionalId=X&date=AAAA-MM-DD
 */
class ProfessionalAgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfessionalAgendaBinding
    private val adapter = AgendaAdapter { appointment -> openDetail(appointment) }

    // Día que se está mostrando (empieza en hoy)
    private val shown: Calendar = Calendar.getInstance()

    private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val prettyFormat = SimpleDateFormat("EEEE d 'de' MMMM", Locale.forLanguageTag("es-CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessionalAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvGreeting.text = "Hola, ${SessionManager.userName}"
        binding.rvAgenda.layoutManager = LinearLayoutManager(this)
        binding.rvAgenda.adapter = adapter

        binding.btnPrev.setOnClickListener { moveDay(-1) }
        binding.btnNext.setOnClickListener { moveDay(1) }
        binding.btnDate.setOnClickListener { pickDate() }
        binding.btnToday.setOnClickListener {
            shown.timeInMillis = System.currentTimeMillis()
            load()
        }
        binding.btnAllAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }
        binding.btnLogout.setOnClickListener { confirmLogout() }
        binding.btnRetry.setOnClickListener { load() }
    }

    // Se recarga al volver del detalle, para ver los cambios (atendida o cancelada)
    override fun onResume() {
        super.onResume()
        load()
    }

    private fun moveDay(days: Int) {
        shown.add(Calendar.DAY_OF_MONTH, days)
        load()
    }

    private fun pickDate() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                shown.set(year, month, day)
                load()
            },
            shown.get(Calendar.YEAR),
            shown.get(Calendar.MONTH),
            shown.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun isToday(): Boolean =
        apiFormat.format(shown.time) == apiFormat.format(Calendar.getInstance().time)

    // Texto del botón de fecha: "Hoy · Sábado 8 de noviembre" o solo la fecha si no es hoy
    private fun updateDateHeader() {
        val pretty = prettyFormat.format(shown.time).replaceFirstChar { it.uppercase() }
        binding.btnDate.text = if (isToday()) "Hoy · $pretty" else pretty
        binding.btnToday.visibility = if (isToday()) View.GONE else View.VISIBLE
    }

    private fun load() {
        updateDateHeader()

        // Una cuenta de profesional siempre debe venir ligada a un perfil
        val professionalId = SessionManager.professionalId
        if (professionalId <= 0) {
            showError("Tu cuenta no está ligada a un perfil de profesional. Cierra sesión e ingresa de nuevo.")
            return
        }

        showLoading()
        val requestedDate = apiFormat.format(shown.time)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAppointments(
                    professionalId = professionalId,
                    date = requestedDate
                )
                // Si mientras cargaba se cambió de día, esta respuesta ya no aplica
                if (requestedDate != apiFormat.format(shown.time)) return@launch

                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    adapter.submit(list)
                    showList(list)
                } else {
                    showError(response.apiError())
                }
            } catch (e: Exception) {
                showError(e.userMessage())
            }
        }
    }

    private fun openDetail(appointment: Appointment) {
        val id = appointment.id ?: return
        startActivity(
            Intent(this, AppointmentDetailActivity::class.java)
                .putExtra(AppointmentDetailActivity.EXTRA_ID, id)
        )
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir?")
            .setPositiveButton("Salir") { _, _ -> SessionManager.logout() }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvAgenda.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.tvSummary.text = ""
    }

    private fun showList(list: List<Appointment>) {
        val isEmpty = list.isEmpty()
        binding.progressBar.visibility = View.GONE
        binding.rvAgenda.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.layoutError.visibility = View.GONE

        // Resumen del día: cuántas citas hay y cuántas siguen pendientes
        val pending = list.count { it.status == "PENDIENTE" }
        binding.tvSummary.text = when {
            isEmpty -> ""
            else -> "${list.size} cita(s) · $pending pendiente(s)"
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvAgenda.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvError.text = message
        binding.tvSummary.text = ""
    }
}