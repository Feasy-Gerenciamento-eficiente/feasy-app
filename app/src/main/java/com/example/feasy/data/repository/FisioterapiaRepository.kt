package com.example.feasy.data.repository

import com.example.feasy.network.SupabaseClient
import com.example.feasy.data.model.Usuario
import com.example.feasy.data.model.Fisioterapeuta
import io.github.jan.supabase.postgrest.from

class FisioterapiaRepository {

    private val supabase = SupabaseClient.client

    suspend fun getUsuarios(): List<Usuario> {
        return supabase.from("usuarios").select().decodeList<Usuario>()
    }

    suspend fun cadastrarFisio(usuario: Usuario, crefito: String, rqe: String?) {
        val usuarioCriado = supabase.from("usuarios")
            .insert(usuario) { select() }
            .decodeSingle<Usuario>()

        val novoId = usuarioCriado.id ?: throw Exception("Erro ao criar ID")

        val fisio = Fisioterapeuta(
            usuarioId = novoId,
            crefito = crefito,
            rqe = rqe
        )
        supabase.from("fisioterapeutas").insert(fisio)
    }
}