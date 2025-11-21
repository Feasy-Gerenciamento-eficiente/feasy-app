package com.example.feasy

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import android.content.Context
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json


object SupabaseClientProvider {

    lateinit var client: SupabaseClient

    // Função para iniciar o Supabase com acesso à memória do celular
    fun init(context: Context) {
        if (::client.isInitialized) return

        // CORREÇÃO: Removi o 'val' daqui.
        // Agora ele atribui para a variável 'client' do Objeto, e não cria uma local.
        client = createSupabaseClient(
            supabaseUrl = "https://ylffveawkvrymekbvbym.supabase.co/",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlsZmZ2ZWF3a3ZyeW1la2J2YnltIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyNjQ0NjMsImV4cCI6MjA3ODg0MDQ2M30.iUOSXOUgUPOlEBk3010CAhdNcKeQrkHh50R6tVMySgk"
        ) {
            install(Auth) {
                // Aqui ensinamos ele a salvar no celular
                sessionManager = AndroidSessionManager(context)
            }
            // CORREÇÃO: Removi o segundo 'install(Auth)' que estava duplicado
            install(Postgrest)
        }
    }
}

// Classe de Sessão (precisa dos imports do Auth e Json acima)
class AndroidSessionManager(context: Context) : SessionManager {

    // Cria um arquivo de preferências chamado "supabase_session"
    private val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)

    override suspend fun saveSession(session: UserSession) {
        // Transforma a sessão em texto (JSON) e salva
        val json = Json.encodeToString(UserSession.serializer(), session)
        prefs.edit().putString("session", json).apply()
    }

    override suspend fun loadSession(): UserSession? {
        // Tenta ler o texto salvo
        val json = prefs.getString("session", null) ?: return null
        return try {
            // Tenta transformar o texto de volta em sessão
            Json.decodeFromString(UserSession.serializer(), json)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        // Apaga tudo (Logout)
        prefs.edit().remove("session").apply()
    }
}