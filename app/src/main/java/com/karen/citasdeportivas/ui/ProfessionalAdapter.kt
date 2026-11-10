package com.karen.citasdeportivas.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karen.citasdeportivas.databinding.ItemProfessionalCardBinding
import com.karen.citasdeportivas.model.Professional

// Adaptador del RecyclerView que muestra la lista de profesionales como tarjetas.
// onClick se ejecuta al tocar una tarjeta (por defecto no hace nada).
class ProfessionalAdapter(
    private val onClick: (Professional) -> Unit = {}
) : RecyclerView.Adapter<ProfessionalAdapter.ViewHolder>() {

    // Lista de profesionales que se están mostrando
    private val items = mutableListOf<Professional>()

    // Reemplaza la lista y redibuja el RecyclerView
    fun submit(list: List<Professional>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // Guarda las vistas de una tarjeta (ViewBinding)
    class ViewHolder(val binding: ItemProfessionalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Crea una tarjeta nueva a partir del layout XML
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemProfessionalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    // Cantidad de tarjetas a mostrar
    override fun getItemCount(): Int = items.size

    // Llena la tarjeta con los datos del profesional de esa posición
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val professional = items[position]
        with(holder.binding) {
            tvName.text = professional.name
            tvSpecialty.text = professional.specialty
            tvSchedule.text =
                "Atiende de ${professional.attentionStart} a ${professional.attentionEnd}"
            // Al tocar la tarjeta se envía el profesional seleccionado
            root.setOnClickListener { onClick(professional) }
        }
    }
}