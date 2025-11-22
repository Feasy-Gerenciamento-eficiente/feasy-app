package com.example.feasy.ui

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

        // --- CORREÇÃO: Usando os IDs corretos do seu XML ---
        // XML: @+id/btnEntrar -> Kotlin: binding.btnEntrar
        binding.btnEntrar.setOnClickListener {

            // XML: @+id/email_input -> Kotlin: binding.emailInput
            val email = binding.emailInput.text.toString()

            // XML: @+id/password_input -> Kotlin: binding.passwordInput
            val senha = binding.passwordInput.text.toString()

            if (email.isBlank() || senha.isBlank()) {
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                }
            }
        }

        // --- CORREÇÃO: Botão de Cadastrar ---
        // XML: @+id/btnIrParaCadastro -> Kotlin: binding.btnIrParaCadastro
        binding.btnIrParaCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }
}