package com.dradefire.securepinonapp.di

import com.dradefire.securepinonapp.ui.main.MainViewModel
import com.dradefire.securepinonapp.ui.confirm.ConfirmViewModel
import com.dradefire.securepinonapp.repository.SharedPreferencesRepository
import com.google.gson.Gson
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val KoinModule = module {
    viewModelOf(::ConfirmViewModel)
    viewModelOf(::MainViewModel)

    factoryOf(::SharedPreferencesRepository)

    singleOf(::Gson)
}