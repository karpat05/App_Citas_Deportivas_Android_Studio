package com.karen.citasdeportivas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.karen.citasdeportivas.databinding.ActivityLoginBinding
import com.karen.citasdeportivas.model.LoginRequest
import com.karen.citasdeportivas.model.RegisterRequest
import com.karen.citasdeportivas.network.CONNECTION_ERROR
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import kotlinx.coroutines.launch

/** Pantalla de inicio de sesión y de registro (se alternan con el botón de abajo). */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var registerMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay una sesión guardada, entra directo a la pantalla de su rol
        if (SessionManager.isLoggedIn) {
            goHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener { submit() }
        binding.btnToggle.setOnClickListener { setMode(!registerMode) }
        setMode(false)
    }

    private fun setMode(register: Boolean) {
        registerMode = register
        binding.tvTitle.text = if (register) "Crear cuenta" else "Iniciar sesión"
        binding.etName.visibility = if (register) View.VISIBLE else View.GONE
        binding.btnSubmit.text = if (register) "Registrarme" else "Entrar"
        binding.btnToggle.text =
            if (register) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate"
        binding.tvFormError.visibility = View.GONE
    }

    private fun submit() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty() || (registerMode && name.isEmpty())) {
            showError("Completa todos los campos")
            return
        }

        setBusy(true)
        lifecycleScope.launch {
            try {
                val response = if (registerMode) {
                    RetrofitClient.api.register(RegisterRequest(name, email, password))
                } else {
                    RetrofitClient.api.login(LoginRequest(email, password))
                }
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    SessionManager.save(body.token, body.user)
                    goHome()
                } else {
                    setBusy(false)
                    showError(response.apiError())
                }
            } catch (e: Exception) {
                setBusy(false)
                showError(CONNECTION_ERROR)
            }
        }
    }

    // ADMIN entra al panel de administración; USER entra a "Mis citas"
    private fun goHome() {
        val target = if (SessionManager.isAdmin) {
            MainActivity::class.java
        } else {
            AppointmentsActivity::class.java
        }
        startActivity(Intent(this, target))
        finish()
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !busy
        binding.btnToggle.isEnabled = !busy
        if (busy) binding.tvFormError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvFormError.text = message
        binding.tvFormError.visibility = View.VISIBLE
    }
}