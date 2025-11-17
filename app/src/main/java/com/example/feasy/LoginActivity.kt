package com.example.feasy.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.feasy.MainActivity
import com.example.feasy.databinding.ActivityLoginBinding
import com.example.feasy.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString()
            val senha = passwordEditText.text.toString()

            if (email.isBlank() || senha.isBlank()) {
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostra o loading
            loadingProgressBar.visibility = android.view.View.VISIBLE

            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = senha
                    }

                    Toast.makeText(applicationContext, "Login realizado!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    loadingProgressBar.visibility = android.view.View.GONE
                }
            }
        }
    }
}