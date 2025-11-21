package com.example.feasy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Representa a tabela 'usuarios' da imagem
@Serializable
data class UsuarioDto(
    // Não mandamos o ID na criação se ele for gerado automaticamente (uuid_generate_v4())
    // Se você tiver que gerar o ID no app, me avise. Vou assumir que o banco gera.
    val id: String? = null,
    val nome: String,
    val email: String, // Email do responsável
    @SerialName("data_nascimento") val dataNascimento: String, // Formato YYYY-MM-DD
    @SerialName("tipo_usuario") val tipoUsuario: String = "paciente"
    // RG você não pediu no form, mas se tiver, coloque aqui
)

// Representa a tabela 'pacientes' da imagem
@Serializable
data class PacienteDto(
    @SerialName("usuario_id") val usuarioId: String, // Chave estrangeira
    @SerialName("fisioterapeuta_id") val fisioterapeutaId: String, // Chave estrangeira
    @SerialName("diagnostico_inicial") val diagnosticoInicial: String,
    @SerialName("acompanhante_emergencia") val acompanhante: String // Ajustei o nome baseado na imagem
)