package com.karen.citasdeportivas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida (HU-01): muestra el logo con una animación corta
 * y pasa al login. Si ya hay sesión, el login redirige solo a la pantalla del rol.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.view.View>(R.id.ivLogo)
        val name = findViewById<android.view.View>(R.id.tvAppName)
        val tagline = findViewById<android.view.View>(R.id.tvTagline)

        // Estado inicial de la animación
        logo.alpha = 0f
        logo.scaleX = 0.6f
        logo.scaleY = 0.6f
        name.alpha = 0f
        name.translationY = 40f
        tagline.alpha = 0f

        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700).start()
        name.animate().alpha(1f).translationY(0f).setStartDelay(400).setDuration(600).start()
        tagline.animate().alpha(1f).setStartDelay(800).setDuration(600).start()

        lifecycleScope.launch {
            delay(2200)
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }
}