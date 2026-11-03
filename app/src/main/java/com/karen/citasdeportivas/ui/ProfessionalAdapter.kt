package com.karen.citasdeportivas.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karen.citasdeportivas.databinding.ItemProfessionalCardBinding
import com.karen.citasdeportivas.model.Professional

class ProfessionalAdapter(
    private val onClick: (Professional) -> Unit = {}
) : RecyclerView.Adapter<ProfessionalAdapter.ViewHolder>() {

    private val items = mutableListOf<Professional>()

    fun submit(list: List<Professional>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemProfessionalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemProfessionalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val professional = items[position]
        with(holder.binding) {
            tvName.text = professional.name
            tvSpecialty.text = professional.specialty
            tvSchedule.text =
                "Atiende de ${professional.attentionStart} a ${professional.attentionEnd}"
            root.setOnClickListener { onClick(professional) }
        }
    }
}