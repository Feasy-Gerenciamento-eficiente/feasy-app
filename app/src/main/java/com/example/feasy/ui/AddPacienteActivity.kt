package com.example.feasy.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.feasy.SupabaseClientProvider
import com.example.feasy.databinding.AddpacienteBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AddPacienteActivity : AppCompatActivity() {

    // O nome do Binding vem do seu XML (addpaciente.xml -> AddpacienteBinding)
    private lateinit var binding: AddpacienteBinding
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddpacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão 'X' de fechar
        binding.buttonClose.setOnClickListener { finish() }

        // Botão Cancelar
        binding.buttonCancelar.setOnClickListener { finish() }

        // Botão "New Pacient" (Salvar)
        binding.btnAddNewPatient.setOnClickListener {
            salvarPaciente()
        }
    }

    private fun salvarPaciente() {
        // 1. Pegar os dados que o usuário digitou
        // Note que o ID 'editTextNomeresptext' é o do Responsável
        val nome = binding.editTextNome.text.toString().trim()
        val nomeResponsavel = binding.editTextNomeresptext.text.toString().trim()
        val email = binding.emailInputtlab.text.toString().trim()
        val dataNascInput = binding.editTextDataNascimento.text.toString().trim() // Esperado: DD/MM/AAAA
        val diagnostico = binding.editTextDiagnostico.text.toString().trim()

        // 2. Validação simples
        if (nome.isEmpty() || email.isEmpty() || dataNascInput.isEmpty() || nomeResponsavel.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Verificar autenticação da Fisio
        val user = SupabaseClientProvider.client.auth.currentUserOrNull()
        if (user == null) {
            Toast.makeText(this, "Erro: Fisioterapeuta não autenticada.", Toast.LENGTH_LONG).show()
            return
        }
        val idFisioLogada = user.id

        // 4. Arrumar a data (De 21/05/1990 para 1990-05-21)
        val dataFormatada = try {
            if (dataNascInput.contains("/")) {
                val partes = dataNascInput.split("/")
                "${partes[2]}-${partes[1]}-${partes[0]}"
            } else {
                dataNascInput
            }
        } catch (e: Exception) {
            dataNascInput // Se der erro, tenta mandar como está
        }

        scope.launch {
            try {
                // --- PARTE A: CRIAR O USUÁRIO NA TABELA 'USUARIOS' ---
                val novoUsuario = UsuarioDto(
                    nome = nome,
                    email = email,
                    dataNascimento = dataFormatada,
                    tipoUsuario = "paciente"
                )

                // Insere e pede para retornar o dado criado (select) para pegarmos o ID novo
                val usuarioCriado = SupabaseClientProvider.client
                    .from("usuarios")
                    .insert(novoUsuario) {
                        select()
                    }.decodeSingle<UsuarioDto>()

                val novoIdUsuario = usuarioCriado.id ?: throw Exception("ID não gerado pelo banco")

                // --- PARTE B: CRIAR O VÍNCULO NA TABELA 'PACIENTES' ---
                val dadosPaciente = PacienteDto(
                    usuarioId = novoIdUsuario,
                    fisioterapeutaId = idFisioLogada,
                    diagnostico = diagnostico,
                    acompanhante = nomeResponsavel // Aqui conecta o Responsável -> Acompanhante
                )

                SupabaseClientProvider.client
                    .from("pacientes")
                    .insert(dadosPaciente)

                // Deu tudo certo!
                Toast.makeText(this@AddPacienteActivity, "Paciente cadastrado!", Toast.LENGTH_SHORT).show()
                finish() // Fecha a tela e volta para a lista

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AddPacienteActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// --- MODELOS DE DADOS (DTOs) ---
// Colocamos aqui embaixo para facilitar, mas poderiam estar em outro arquivo.

@Serializable
data class UsuarioDto(
    val id: String? = null,
    val nome: String,
    val email: String,
    @SerialName("data_nascimento") val dataNascimento: String,
    @SerialName("tipo_usuario") val tipoUsuario: String
)

@Serializable
data class PacienteDto(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("fisioterapeuta_id") val fisioterapeutaId: String,
    @SerialName("diagnostico_inicial") val diagnostico: String,
    // Aqui garantimos que o 'responsable name' vai para a coluna certa do banco:
    @SerialName("acompanhante_emergencia") val acompanhante: String
)