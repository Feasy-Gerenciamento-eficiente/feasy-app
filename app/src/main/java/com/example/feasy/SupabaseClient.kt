package com.example.feasy

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClientProvider {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://ylffveawkvrymekbvbym.supabase.co/",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlsZmZ2ZWF3a3ZyeW1la2J2YnltIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyNjQ0NjMsImV4cCI6MjA3ODg0MDQ2M30.iUOSXOUgUPOlEBk3010CAhdNcKeQrkHh50R6tVMySgk"
    ) {
        install(Auth)
        install(Postgrest)
    }
}


