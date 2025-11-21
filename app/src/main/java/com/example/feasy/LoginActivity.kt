package com.example.feasy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.feasy.databinding.ActivityLoginBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

// IMPORTANTE: Se sua SignupActivity estiver na pasta 'ui', precisa desse import:
import com.example.feasy.ui.SignupActivity
// Se a linha acima ficar vermelha, apague ela e importe 'SignupActivity' manualmente (Alt+Enter).

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Lógica do Botão ENTRAR ---
        binding.btnEntrar.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            scope.launch {
                try {
                    SupabaseClientProvider.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    Toast.makeText(this@LoginActivity, "Login realizado!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@LoginActivity, PacientsActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // --- Lógica do Botão CADASTRAR (Agora no lugar certo!) ---
        // Certifique-se que no seu XML o ID é @+id/signupLink
        binding.signupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

    } // <--- O onCreate fecha AQUI

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Evita travar o app se sair da tela durante o login
    }
} // <--- A classe fecha AQUI