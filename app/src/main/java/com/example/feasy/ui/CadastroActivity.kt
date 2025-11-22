package com.example.feasy.ui // Verifique se o pacote é esse mesmo

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

        // Lógica do botão "Finalizar Cadastro" (que está no activity_cadastro.xml)
        binding.btnFinalizarCadastro.setOnClickListener {
            val nome = binding.etNome.text.toString()
            val email = binding.etEmailCadastro.text.toString()
            val senha = binding.etSenhaCadastro.text.toString()

            // Validações simples
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

    private fun realizarCadastro(nome: String, email: String, senha: String) {
        binding.pbCadastro.visibility = android.view.View.VISIBLE
        binding.btnFinalizarCadastro.isEnabled = false

        lifecycleScope.launch {
            try {
                // 1. Cria o usuário no Auth do Supabase
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = senha
                }

                // 2. Pega o ID do usuário recém-criado da sessão
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val novoId = user?.id ?: throw Exception("Erro ao recuperar ID do usuário criado")

                // 3. Salva os dados na sua tabela 'usuarios'
                val novoUsuarioBanco = Usuario(
                    id = novoId,
                    nome = nome,
                    email = email,
                    rg = null,
                    dataNascimento = null,
                    tipoUsuario = "fisioterapeuta" // Define o tipo padrão
                )

                SupabaseClient.client.from("usuarios").insert(novoUsuarioBanco)

                // 4. SUCESSO! (Como você pediu)
                Toast.makeText(applicationContext, "Cadastro realizado! Faça o login.", Toast.LENGTH_LONG).show()

                // 5. Volta para a tela de Login
                finish()

            } catch (e: Exception) {
                // 6. ERRO! (Como você pediu)
                e.printStackTrace()
                Toast.makeText(applicationContext, "Erro no cadastro: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.pbCadastro.visibility = android.view.View.GONE
                binding.btnFinalizarCadastro.isEnabled = true
            }
        }
    }
}