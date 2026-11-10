package com.karen.citasdeportivas.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karen.citasdeportivas.databinding.ItemAgendaBinding
import com.karen.citasdeportivas.model.Appointment

// Adaptador de la agenda del profesional: cada cita muestra hora, deportista, motivo y estado.
// onClick se ejecuta al tocar una cita (abre su detalle).
class AgendaAdapter(
    private val onClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AgendaAdapter.ViewHolder>() {

    private val items = mutableListOf<Appointment>()

    // Reemplaza la lista, ordenada por hora de inicio (HH:MM se ordena bien como texto)
    fun submit(list: List<Appointment>) {
        items.clear()
        items.addAll(list.sortedBy { it.startTime })
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemAgendaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAgendaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = items[position]
        // Verde: atendida, rojo: cancelada, naranja: pendiente
        val statusColor = when (appointment.status) {
            "ATENDIDA" -> "#1B6E3A"
            "CANCELADA" -> "#B3261E"
            else -> "#B26A00"
        }
        with(holder.binding) {
            tvStart.text = appointment.startTime
            tvEnd.text = "a ${appointment.endTime}"
            tvAthlete.text = appointment.athleteName
            tvReason.text = appointment.reason
            tvStatus.text = appointment.status
            tvStatus.setTextColor(Color.parseColor(statusColor))
            root.setOnClickListener { onClick(appointment) }
        }
    }
}