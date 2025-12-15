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

    // ADICIONADO - Variáveis para edição de pacientes
    private var usuarioIdParaEdicao: String? = null

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddpacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // ADICIONADO ---  🔹 PASSO 1: verificar se veio ID para edição
        usuarioIdParaEdicao = intent.getStringExtra("USUARIO_ID")

        // ADICIONADO ---
        if (usuarioIdParaEdicao != null) {
            preencherCamposEdicao()
        }


        // Botão 'X' de fechar
        binding.buttonClose.setOnClickListener { finish() }

        // Botão Cancelar
        binding.buttonCancelar.setOnClickListener { finish() }

        // ADICIONADO ---- Botão "New Pacient" (Salvar)
        binding.btnAddNewPatient.setOnClickListener {
            if (usuarioIdParaEdicao == null) {
                salvarPaciente()       // criação
            } else {
                atualizarPaciente()   // edição
            }
        }

    }

    //ADICIONADO ---
    private fun atualizarPaciente() {

        val usuarioId = usuarioIdParaEdicao ?: return

        // 1. Pega os dados digitados
        val nome = binding.editTextNome.text.toString().trim()
        val nomeResponsavel = binding.editTextNomeresptext.text.toString().trim()
        val email = binding.emailInputtlab.text.toString().trim()
        val dataNascInput = binding.editTextDataNascimento.text.toString().trim()
        val diagnostico = binding.editTextDiagnostico.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || dataNascInput.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Converte data para YYYY-MM-DD
        val dataFormatada = try {
            val partes = dataNascInput.split("/")
            "${partes[2]}-${partes[1]}-${partes[0]}"
        } catch (e: Exception) {
            dataNascInput
        }

        scope.launch {
            try {
                // --- A) ATUALIZA USUÁRIO ---
                SupabaseClientProvider.client
                    .from("usuarios")
                    .update(
                        mapOf(
                            "nome" to nome,
                            "email" to email,
                            "data_nascimento" to dataFormatada
                        )
                    ) {
                        filter {
                            eq("id", usuarioId)
                        }
                    }

                // --- B) ATUALIZA PACIENTE ---
                SupabaseClientProvider.client
                    .from("pacientes")
                    .update(
                        mapOf(
                            "diagnostico_inicial" to diagnostico,
                            "acompanhante_emergencia" to nomeResponsavel
                        )
                    ) {
                        filter {
                            eq("usuario_id", usuarioId)
                        }
                    }

                Toast.makeText(
                    this@AddPacienteActivity,
                    "Paciente atualizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                finish() // fecha o modal

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@AddPacienteActivity,
                    "Erro ao atualizar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
























    // ADICIONADO ---
    private fun entrarModoEdicao() {
        // 🔹 muda o título
        binding.textViewTitle.text = "Editar Paciente"

        // 🔹 muda o texto do botão
        binding.btnAddNewPatient.text = "Salvar Alterações"
    }

    //ADICIONADO ---
    private fun preencherCamposEdicao() {

        binding.editTextNome.setText(
            intent.getStringExtra("NOME") ?: ""
        )

        binding.editTextNomeresptext.setText(
            intent.getStringExtra("RESPONSAVEL") ?: ""
        )

        binding.emailInputtlab.setText(
            intent.getStringExtra("EMAIL") ?: ""
        )

        binding.editTextDataNascimento.setText(
            intent.getStringExtra("DATA_NASC") ?: ""
        )

        binding.editTextDiagnostico.setText(
            intent.getStringExtra("DIAGNOSTICO") ?: ""
        )

        // muda o texto do botão
        binding.btnAddNewPatient.text = "Salvar alterações"
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