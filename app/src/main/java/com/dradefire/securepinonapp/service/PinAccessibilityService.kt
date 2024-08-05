package com.dradefire.securepinonapp.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.provider.Settings.ACTION_SETTINGS
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import com.dradefire.securepinonapp.R
import com.dradefire.securepinonapp.receiver.AdminReceiver
import com.dradefire.securepinonapp.repository.SharedPreferencesRepository
import com.dradefire.securepinonapp.ui.confirm.ConfirmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PinAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val event = MutableSharedFlow<AccessibilityEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val spRepository by inject<SharedPreferencesRepository>()
    private var packageForPinList = spRepository.packageIdList

    private var lastPinnedAppPackageName: String? = null
    private var settingsList = listOf<String>()

    private val dpm by lazy { getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    private val adminReceiver by lazy {
        ComponentName(
            applicationContext,
            AdminReceiver::class.java
        )
    }

    private val isPinCodeNotExist
        get() = spRepository.pinCode.isNullOrEmpty()
    private val isCorrectPin
        get() = spRepository.isCorrectPin == true

    init {
        scope.launch {
            while (isActive) {
                delay(15_000)
                if (dpm.isAdminActive(adminReceiver)) {
                    val intent = Intent(ACTION_SETTINGS)
                    settingsList = packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_ALL)
                        .distinctBy { it.activityInfo.packageName }
                        .map { it.activityInfo.packageName }
                }
            }
        }

        scope.launch {
            while (isActive) {
                delay(4000)
                packageForPinList = spRepository.packageIdList + settingsList
            }
        }

        event
            .filter { event ->
                !isPinCodeNotExist && !isCorrectPin &&
                        event.eventType != TYPE_NOTIFICATION_STATE_CHANGED || event.packageName != lastPinnedAppPackageName
            }
            .onEach { event ->
                spRepository.isCorrectPin = false
                lastPinnedAppPackageName = event.packageName.toString()

                val startActivityIntent = Intent(this, ConfirmActivity::class.java)
                    .apply {
                        setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    or Intent.FLAG_ACTIVITY_NO_ANIMATION
                                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
                        )
                    }
                startActivity(startActivityIntent)
            }.catch {
                Log.e("ERROR", it.message, it)
            }.launchIn(scope)
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    private fun startForeground() {
        val channelId = createNotificationChannel()

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pin On App")
            .setContentText("Apps protected")
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    private fun createNotificationChannel(): String {
        val channelId = "pin_on_app_service"
        val channelName = "PinOnApp"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)

        return channelId
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null) {
            Log.d("NEW_EVENT", event.toString())

            if (packageForPinList.contains(event.packageName) || event.isMainActivityShowed()) {
                this.event.tryEmit(event)
            }
        }
    }

    private fun AccessibilityEvent.isMainActivityShowed() =
        className?.contains(APP_CLASS_NAME) ?: false

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val APP_CLASS_NAME = "com.dradefire.securepinonapp.ui.main.MainActivity"
    }
}