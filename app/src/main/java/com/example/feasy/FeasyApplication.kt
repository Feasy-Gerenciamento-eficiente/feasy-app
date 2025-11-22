package com.example.feasy

import android.app.Application

class FeasyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(this)
    }
}