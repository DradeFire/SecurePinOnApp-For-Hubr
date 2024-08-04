package com.dradefire.securepinonapp.repository

import android.content.Context
import com.google.gson.Gson

class SharedPreferencesRepository(
    private val context: Context,
    private val gson: Gson,
) {
    /**
     * Был ли введён правильный пин-код (нужно, чтобы лишний раз не показывать экран блокировки)
     */
    var isCorrectPin: Boolean?
        get() = context.sp.getBoolean(IS_CORRECT_PIN_KEY, false)
        set(isCorrectPin) {
            context.sp.edit().putBoolean(IS_CORRECT_PIN_KEY, isCorrectPin ?: false).apply()
        }

    /**
     * Пин-код
     */
    var pinCode: String?
        get() = context.sp.getString(PIN_KEY, null)
        set(pinCode) {
            context.sp.edit().putString(PIN_KEY, pinCode).apply()
        }

    /**
     * Список приложений, которые нужно защитить пин-кодом
     */
    var packageIdList: List<String>
        get() = gson.fromJson(
            context.sp.getString(
                PACKAGE_ID_LIST_KEY,
                gson.toJson(emptyList<String>()),
            ),
            List::class.java,
        ) as List<String>
        set(list) {
            context.sp.edit().putString(PACKAGE_ID_LIST_KEY, gson.toJson(list)).apply()
        }

    private val Context.sp
        get() = getSharedPreferences(SECURE_PIN_ON_APP_STORAGE, Context.MODE_PRIVATE)

    companion object {
        private const val PACKAGE_ID_LIST_KEY = "PACKAGE_ID_LIST"
        private const val PIN_KEY = "PIN"
        private const val IS_CORRECT_PIN_KEY = "IS_CORRECT_PIN"
        private const val SECURE_PIN_ON_APP_STORAGE = "secure_pin_on_app_storage"
    }
}