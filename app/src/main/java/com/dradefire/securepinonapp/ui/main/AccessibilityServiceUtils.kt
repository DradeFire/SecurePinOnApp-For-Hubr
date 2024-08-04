package com.dradefire.securepinonapp.ui.main

import android.content.Context
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import com.dradefire.securepinonapp.service.PinAccessibilityService

// Copied from https://mhrpatel12.medium.com/android-accessibility-service-the-unexplored-goldmine-d336b0f33e30
fun Context.isAccessibilityServiceEnabled(): Boolean {
    var accessibilityEnabled = 0
    val service: String = packageName + "/" + PinAccessibilityService::class.java.canonicalName
    try {
        accessibilityEnabled = Settings.Secure.getInt(
            applicationContext.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
        )

        Log.v("ACCESSIBILITY_ENABLED_LOG", "accessibilityEnabled = $accessibilityEnabled")
    } catch (e: SettingNotFoundException) {
        Log.e(
            "ACCESSIBILITY_ENABLED_LOG",
            "Error finding setting, default accessibility to not found: " + e.message,
        )
    }
    val mStringColonSplitter = SimpleStringSplitter(':')
    if (accessibilityEnabled == 1) {
        Log.v("ACCESSIBILITY_ENABLED_LOG", "Accessibility Is Enabled")
        val settingValue: String = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        )
        mStringColonSplitter.setString(settingValue)
        while (mStringColonSplitter.hasNext()) {
            val accessibilityService = mStringColonSplitter.next()
            Log.v(
                "ACCESSIBILITY_ENABLED_LOG",
                "AccessibilityService :: $accessibilityService $service"
            )
            if (accessibilityService.equals(service, ignoreCase = true)) {
                Log.v("ACCESSIBILITY_ENABLED_LOG", "accessibility is switched on!")
                return true
            }
        }
    } else {
        Log.v("ACCESSIBILITY_ENABLED_LOG", "accessibility is disabled")
    }
    return false
}
