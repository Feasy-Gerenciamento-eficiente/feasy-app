package com.example.feasy.ui // CORRIGIDO: Estava ui.login, mas o arquivo está em ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.feasy.databinding.ActivityCadastroBinding
import com.example.feasy.network.SupabaseClient
import com.example.feasy.data.model.Usuario
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFinalizarCadastro.setOnClickListener {
            val nome = binding.etNome.text.toString()
            val email = binding.etEmailCadastro.text.toString()
            val senha = binding.etSenhaCadastro.text.toString()

            if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha.length < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            realizarCadastro(nome, email, senha)
        }
    }

    private fun realizarCadastro(nome: String, emailInput: String, senhaInput: String) {
        binding.pbCadastro.visibility = android.view.View.VISIBLE
        binding.btnFinalizarCadastro.isEnabled = false

        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = senhaInput
                }

                val user = SupabaseClient.client.auth.currentUserOrNull()
                val novoId = user?.id ?: throw Exception("Erro: Usuário criado mas ID não encontrado. Verifique confirmação de email.")

                val novoUsuarioBanco = Usuario(
                    id = novoId,
                    nome = nome,
                    email = emailInput,
                    rg = null,
                    dataNascimento = null,
                    tipoUsuario = "fisioterapeuta"
                )

                SupabaseClient.client.from("usuarios").insert(novoUsuarioBanco)

                Toast.makeText(applicationContext, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()
                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.pbCadastro.visibility = android.view.View.GONE
                binding.btnFinalizarCadastro.isEnabled = true
            }
        }
    }
}