package com.example.feasy

import android.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Sub-molde para os dados da tabela 'usuarios'
@Serializable
data class UsuarioInfo(
    val nome: String,
    @SerialName("data_nascimento")
    val dataNascimento: String
)

// Molde principal
@Serializable
data class Paciente(
    // Dados da tabela 'pacientes'
    val id: Int,

    @SerialName("diagnostico_inicial")
    val diagnostico: String,

    // ⚠️ AÇÃO: Crie esta coluna 'cor_avatar' (tipo text) na sua tabela 'pacientes'
    @SerialName("cor_avatar")
    val corDoAvatar: String,

    // Objeto aninhado que recebe os dados do JOIN
    // O nome da variável 'usuarios' DEVE ser o nome da tabela no Supabase
    val usuarios: UsuarioInfo
) {

    // Pega o nome de dentro do objeto 'usuarios'
    val iniciais: String
        get() {
            val partesDoNome = usuarios.nome.split(" ").filter { it.isNotBlank() }
            val primeiraLetra = partesDoNome.firstOrNull()?.take(1) ?: ""
            val ultimaLetra = partesDoNome.lastOrNull()?.take(1) ?: ""
            if (partesDoNome.size == 1) {
                return usuarios.nome.take(2).uppercase()
            }
            return (primeiraLetra + ultimaLetra).uppercase()
        }

    // Converte a cor Hex (#FFFFFF) do banco em Int
    val corDoAvatarInt: Int
        get() {
            try {
                return Color.parseColor(corDoAvatar)
            } catch (e: Exception) {
                return Color.rgb(100, 100, 100) // Cor padrão em caso de erro
            }
        }
}