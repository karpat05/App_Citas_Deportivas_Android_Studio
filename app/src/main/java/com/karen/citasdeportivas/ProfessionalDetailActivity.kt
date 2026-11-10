package com.karen.citasdeportivas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.karen.citasdeportivas.databinding.ActivityProfessionalDetailBinding
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import com.karen.citasdeportivas.network.userMessage
import kotlinx.coroutines.launch

class ProfessionalDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "professional_id"
    }

    private lateinit var binding: ActivityProfessionalDetailBinding
    private var professionalId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessionalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        professionalId = intent.getLongExtra(EXTRA_ID, -1L)

        binding.btnRetry.setOnClickListener { load() }
        binding.btnEdit.setOnClickListener {
            startActivity(
                Intent(this, ProfessionalFormActivity::class.java)
                    .putExtra(ProfessionalFormActivity.EXTRA_ID, professionalId)
            )
        }
        binding.btnSchedule.setOnClickListener {
            startActivity(
                Intent(this, AppointmentFormActivity::class.java)
                    .putExtra(AppointmentFormActivity.EXTRA_PROFESSIONAL_ID, professionalId)
            )
        }
        binding.btnViewAppointments.setOnClickListener {
            startActivity(
                Intent(this, AppointmentsActivity::class.java)
                    .putExtra(AppointmentsActivity.EXTRA_PROFESSIONAL_ID, professionalId)
            )
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    // Se recarga al volver, para que se vean los cambios hechos en el formulario de edición
    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getProfessional(professionalId)
                val professional = response.body()
                if (response.isSuccessful && professional != null) {
                    binding.tvName.text = professional.name
                    binding.tvSpecialty.text = professional.specialty
                    binding.tvSchedule.text =
                        "Atiende de ${professional.attentionStart} a ${professional.attentionEnd}"
                    showContent()
                } else {
                    showError(response.apiError())
                }
            } catch (e: Exception) {
                showError(e.userMessage())
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar profesional")
            .setMessage("¿Seguro que quieres eliminar a ${binding.tvName.text}?")
            .setPositiveButton("Eliminar") { _, _ -> delete() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun delete() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.deleteProfessional(professionalId)
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ProfessionalDetailActivity,
                        "Profesional eliminado",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    // Ejemplo: 409 "No se puede eliminar: el profesional tiene citas registradas"
                    Toast.makeText(
                        this@ProfessionalDetailActivity,
                        response.apiError(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ProfessionalDetailActivity,
                    e.userMessage(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.layoutContent.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvError.text = message
    }
}