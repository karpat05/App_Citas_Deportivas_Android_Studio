package com.karen.citasdeportivas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.karen.citasdeportivas.databinding.ActivityProfessionalFormBinding
import com.karen.citasdeportivas.model.Professional
import com.karen.citasdeportivas.network.CONNECTION_ERROR
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import com.karen.citasdeportivas.ui.setupTimePicker
import kotlinx.coroutines.launch

/** Sirve para crear (POST) y para editar (PUT) un profesional. */
class ProfessionalFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "professional_id"
    }

    private lateinit var binding: ActivityProfessionalFormBinding
    private var editingId: Long = -1L
    private val isEditing: Boolean get() = editingId > 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessionalFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getLongExtra(EXTRA_ID, -1L)

        binding.tvFormTitle.text = if (isEditing) "Editar profesional" else "Nuevo profesional"
        binding.etStart.setupTimePicker()
        binding.etEnd.setupTimePicker()
        binding.btnSave.setOnClickListener { save() }

        if (isEditing) loadExisting()
    }

    private fun loadExisting() {
        setSaving(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getProfessional(editingId)
                val professional = response.body()
                if (response.isSuccessful && professional != null) {
                    binding.etName.setText(professional.name)
                    binding.etSpecialty.setText(professional.specialty)
                    binding.etStart.setText(professional.attentionStart)
                    binding.etEnd.setText(professional.attentionEnd)
                } else {
                    showFormError(response.apiError())
                }
            } catch (e: Exception) {
                showFormError(CONNECTION_ERROR)
            }
            setSaving(false)
        }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val specialty = binding.etSpecialty.text.toString().trim()
        val start = binding.etStart.text.toString().trim()
        val end = binding.etEnd.text.toString().trim()

        if (name.isEmpty() || specialty.isEmpty() || start.isEmpty() || end.isEmpty()) {
            showFormError("Completa todos los campos")
            return
        }

        val professional = Professional(
            name = name,
            specialty = specialty,
            attentionStart = start,
            attentionEnd = end
        )

        setSaving(true)
        lifecycleScope.launch {
            try {
                val response = if (isEditing) {
                    RetrofitClient.api.updateProfessional(editingId, professional)
                } else {
                    RetrofitClient.api.createProfessional(professional)
                }
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ProfessionalFormActivity,
                        if (isEditing) "Profesional actualizado" else "Profesional creado",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    setSaving(false)
                    showFormError(response.apiError())
                }
            } catch (e: Exception) {
                setSaving(false)
                showFormError(CONNECTION_ERROR)
            }
        }
    }

    private fun setSaving(saving: Boolean) {
        binding.progressBar.visibility = if (saving) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !saving
        if (saving) binding.tvFormError.visibility = View.GONE
    }

    private fun showFormError(message: String) {
        binding.tvFormError.text = message
        binding.tvFormError.visibility = View.VISIBLE
    }
}