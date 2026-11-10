package com.karen.citasdeportivas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.karen.citasdeportivas.databinding.ActivityMainBinding
import com.karen.citasdeportivas.model.Professional
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import com.karen.citasdeportivas.network.userMessage
import com.karen.citasdeportivas.ui.ProfessionalAdapter
import kotlinx.coroutines.launch

/**
 * Lista de profesionales.
 * ADMIN: puede crear profesionales y entrar a la lista de todas las citas.
 * USER: solo consulta (los botones de administración se ocultan).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = ProfessionalAdapter { professional -> openDetail(professional) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleLabel = if (SessionManager.isAdmin) "Administrador" else "Usuario"
        binding.tvWelcome.text = "Hola, ${SessionManager.userName} · $roleLabel"
        if (!SessionManager.isAdmin) binding.layoutActions.visibility = View.GONE

        binding.rvProfessionals.layoutManager = LinearLayoutManager(this)
        binding.rvProfessionals.adapter = adapter

        binding.btnRetry.setOnClickListener { loadProfessionals() }
        binding.btnLogout.setOnClickListener { SessionManager.logout() }
        binding.btnNewProfessional.setOnClickListener {
            startActivity(Intent(this, ProfessionalFormActivity::class.java))
        }
        binding.btnAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }
    }

    // Se recarga cada vez que se vuelve a esta pantalla (después de crear, editar o eliminar)
    override fun onResume() {
        super.onResume()
        loadProfessionals()
    }

    private fun openDetail(professional: Professional) {
        val id = professional.id ?: return
        startActivity(
            Intent(this, ProfessionalDetailActivity::class.java)
                .putExtra(ProfessionalDetailActivity.EXTRA_ID, id)
        )
    }

    private fun loadProfessionals() {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getProfessionals()
                if (response.isSuccessful) {
                    adapter.submit(response.body().orEmpty())
                    showList()
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
        binding.rvProfessionals.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showList() {
        binding.progressBar.visibility = View.GONE
        binding.rvProfessionals.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvProfessionals.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvError.text = message
    }
}