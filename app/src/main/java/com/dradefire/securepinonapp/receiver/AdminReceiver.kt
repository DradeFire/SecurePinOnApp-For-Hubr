package com.dradefire.securepinonapp.receiver

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            ACTION_DEVICE_ADMIN_DISABLED,
            ACTION_DEVICE_ADMIN_DISABLE_REQUESTED, -> {
                val dpm = context.getSystemService(DevicePolicyManager::class.java)
                dpm.lockNow()
            }
        }
    }
}