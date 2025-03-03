package com.dradefire.securepinonapp.application

import android.app.Application
import com.dradefire.securepinonapp.di.KoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(KoinModule)
        }
    }
}