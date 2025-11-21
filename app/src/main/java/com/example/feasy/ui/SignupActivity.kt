package com.example.feasy.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.feasy.databinding.SignupfisioBinding // Certifique-se que o nome do XML é signupfisio.xml
import com.example.feasy.SupabaseClientProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: SignupfisioBinding
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupfisioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão de "Já tem conta? Faça Login" (ID: signup)
        binding.signup.setOnClickListener {
            finish()
        }

        // Botão de CADASTRAR (ID: btnCadastrar)
        binding.btnCadastrar.setOnClickListener {

            // --- AQUI ESTAVAM OS ERROS, AGORA CORRIGIDOS COM SEUS IDs ---

            // No XML: @+id/nameinputt -> Kotlin: binding.nameinputt
            val name = binding.nameinputt.text.toString().trim()

            // No XML: @+id/email_inputt -> Kotlin: binding.emailInputt (CamelCase)
            val email = binding.emailInputt.text.toString().trim()

            // No XML: @+id/password_inputt -> Kotlin: binding.passwordInputt
            val password = binding.passwordInputt.text.toString().trim()

            // No XML: @+id/crefitoo -> Kotlin: binding.crefitoo
            val crefito = binding.crefitoo.text.toString().trim()

            // -----------------------------------------------------------

            // Validações
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || crefito.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chamada ao Supabase
            scope.launch {
                try {
                    SupabaseClientProvider.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password

                        // Enviando Nome e Crefito como metadados
                        data = buildJsonObject {
                            put("full_name", name)
                            put("crefito", crefito)
                            put("role", "fisio")
                        }
                    }

                    Toast.makeText(this@SignupActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@SignupActivity, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}