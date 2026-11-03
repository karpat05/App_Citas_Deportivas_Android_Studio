package com.karen.citasdeportivas.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karen.citasdeportivas.databinding.ItemAppointmentCardBinding
import com.karen.citasdeportivas.model.Appointment

class AppointmentAdapter(
    private val professionalName: (Long) -> String,
    private val onClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    private val items = mutableListOf<Appointment>()

    fun submit(list: List<Appointment>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemAppointmentCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemAppointmentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = items[position]
        val statusColor = when (appointment.status) {
            "ATENDIDA" -> "#1B6E3A"
            "CANCELADA" -> "#B3261E"
            else -> "#B26A00"
        }
        with(holder.binding) {
            tvAthlete.text = appointment.athleteName
            tvProfessional.text = "Profesional: ${professionalName(appointment.professionalId)}"
            tvWhen.text =
                "${appointment.date}  ·  ${appointment.startTime} - ${appointment.endTime}"
            tvStatus.text = appointment.status
            tvStatus.setTextColor(Color.parseColor(statusColor))
            tvReason.text = appointment.reason
            root.setOnClickListener { onClick(appointment) }
        }
    }
}