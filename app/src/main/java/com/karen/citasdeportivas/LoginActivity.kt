package com.karen.citasdeportivas

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.karen.citasdeportivas.databinding.ActivityLoginBinding
import com.karen.citasdeportivas.model.LoginRequest
import com.karen.citasdeportivas.model.RegisterRequest
import com.karen.citasdeportivas.network.RetrofitClient
import com.karen.citasdeportivas.network.apiError
import com.karen.citasdeportivas.network.userMessage
import com.karen.citasdeportivas.ui.clearFieldErrors
import com.karen.citasdeportivas.ui.fail
import com.karen.citasdeportivas.ui.value
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
        clearFieldErrors(binding.etName, binding.etEmail, binding.etPassword)
        val name = binding.etName.value()
        val email = binding.etEmail.value()
        val password = binding.etPassword.text.toString()

        val problem = when {
            registerMode && name.isEmpty() -> binding.etName.fail("Escribe tu nombre")
            email.isEmpty() -> binding.etEmail.fail("Escribe tu correo")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                binding.etEmail.fail("El correo no es válido (ejemplo: nombre@correo.com)")
            password.isEmpty() -> binding.etPassword.fail("Escribe tu contraseña")
            registerMode && password.length < 6 ->
                binding.etPassword.fail("La contraseña debe tener al menos 6 caracteres")
            else -> null
        }
        if (problem != null) {
            showError(problem)
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
                showError(e.userMessage())
            }
        }
    }

    // ADMIN entra al panel de administración, PROFESSIONAL a su agenda y USER a "Mis citas"
    private fun goHome() {
        val target = when {
            SessionManager.isAdmin -> MainActivity::class.java
            SessionManager.isProfessional -> ProfessionalAgendaActivity::class.java
            else -> AppointmentsActivity::class.java
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