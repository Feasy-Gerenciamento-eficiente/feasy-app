package com.example.feasy.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: String? = null,
    val nome: String,
    val email: String,
    val rg: String?,
    @SerialName("data_nascimento") val dataNascimento: String?,
    @SerialName("tipo_usuario") val tipoUsuario: String
)

@Serializable
data class Fisioterapeuta(
    @SerialName("usuario_id") val usuarioId: String,
    val crefito: String,
    val rqe: String?
)

// Adicionei as outras tabelas aqui para facilitar:

@Serializable
data class Paciente(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("fisioterapeuta_id") val fisioterapeutaId: String?,
    @SerialName("diagnostico_inicial") val diagnosticoInicial: String?,
    @SerialName("acompanhante_emergencia") val acompanhanteEmergencia: String?
)

@Serializable
data class Prontuario(
    val id: String? = null,
    @SerialName("paciente_id") val pacienteId: String,
    @SerialName("fisioterapeuta_id") val fisioterapeutaId: String,
    @SerialName("data_inicio_tratamento") val dataInicio: String,
    @SerialName("objetivos_tratamento") val objetivos: String?,
    @SerialName("historico_medico") val historico: String?
)