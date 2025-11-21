package com.example.feasy

import android.app.Application

class FeasyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicia o Supabase assim que o App abre
        SupabaseClientProvider.init(this)
    }
}