package com.karen.citasdeportivas.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karen.citasdeportivas.databinding.ItemAppointmentCardBinding
import com.karen.citasdeportivas.model.Appointment

/**
 * Adaptador del RecyclerView que muestra la lista de citas.
 * Cada cita se dibuja como una tarjeta (item_appointment_card.xml).
 *
 * @param professionalName función que recibe el id de un profesional y devuelve su nombre,
 *                         porque la cita solo guarda el id y no el nombre.
 * @param onClick función que se ejecuta cuando el usuario toca una tarjeta
 *                (normalmente abre el detalle de la cita).
 */
class AppointmentAdapter(
    private val professionalName: (Long) -> String,
    private val onClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    // Lista interna con las citas que se están mostrando en pantalla
    private val items = mutableListOf<Appointment>()

    // Reemplaza la lista actual por una nueva (por ejemplo, la que llegó de la API)
    // y le avisa al RecyclerView que debe redibujarse
    fun submit(list: List<Appointment>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // ViewHolder: guarda las vistas de una tarjeta (usa ViewBinding)
    // para no tener que buscarlas con findViewById cada vez que se reutiliza
    class ViewHolder(val binding: ItemAppointmentCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Se llama cuando el RecyclerView necesita crear una tarjeta nueva:
    // infla el layout XML y lo envuelve en un ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemAppointmentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    // Cantidad total de tarjetas que debe mostrar el RecyclerView
    override fun getItemCount(): Int = items.size

    // Se llama para llenar una tarjeta con los datos de la cita en la posición indicada
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = items[position]

        // Elige el color del estado: verde si fue atendida, rojo si fue cancelada
        // y naranja para cualquier otro estado (por ejemplo, pendiente)
        val statusColor = when (appointment.status) {
            "ATENDIDA" -> "#1B6E3A"
            "CANCELADA" -> "#B3261E"
            else -> "#B26A00"
        }

        with(holder.binding) {
            // Nombre del deportista
            tvAthlete.text = appointment.athleteName

            // Nombre del profesional, obtenido a partir de su id con la función recibida
            tvProfessional.text = "Profesional: ${professionalName(appointment.professionalId)}"

            // Fecha y rango de hora de la cita
            tvWhen.text =
                "${appointment.date}  ·  ${appointment.startTime} - ${appointment.endTime}"

            // Estado de la cita con el color elegido arriba
            tvStatus.text = appointment.status
            tvStatus.setTextColor(Color.parseColor(statusColor))

            // Motivo de la cita
            tvReason.text = appointment.reason

            // Al tocar la tarjeta se avisa a quien usa el adaptador, enviando la cita tocada
            root.setOnClickListener { onClick(appointment) }
        }
    }
}