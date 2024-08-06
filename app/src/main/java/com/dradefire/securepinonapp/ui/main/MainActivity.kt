package com.dradefire.securepinonapp.ui.main

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
import android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION
import android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import com.dradefire.securepinonapp.receiver.AdminReceiver
import com.dradefire.securepinonapp.repository.SharedPreferencesRepository
import com.dradefire.securepinonapp.ui.confirm.ConfirmActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val spRepository by inject<SharedPreferencesRepository>()
    private val viewModel by viewModel<MainViewModel>()
    private val dpm by lazy { getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    private val adminReceiver by lazy {
        ComponentName(
            applicationContext,
            AdminReceiver::class.java
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        checkPermission()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        checkPermission()
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (spRepository.pinCode == null) {
            openConfirmActivityWithSettingPinCode()
        }

        // Достаём список всех приложений, кроме текущего
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val applicationList = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL,
        ).distinctBy { it.activityInfo.packageName }

        val packageIdListInit = spRepository.packageIdList
        val appInfoListInit = applicationList.mapNotNull {
            val activityInfo = it.activityInfo

            if (activityInfo.packageName == APP_PACKAGE_ID) {
                null
            } else {
                ApplicationInfo(
                    icon = activityInfo.applicationInfo.loadIcon(packageManager)
                        .toBitmap(),
                    name = activityInfo.applicationInfo.loadLabel(packageManager)
                        .toString(),
                    packageId = activityInfo.packageName,
                    isSecured = packageIdListInit.contains(activityInfo.packageName),
                )
            }
        }

        setContent {
            MaterialTheme {
                var appInfoList = remember {
                    appInfoListInit.toMutableStateList()
                }

                val isAccessibilityGranted by viewModel.isAccessibilityGranted.collectAsState()
                val isNotificationGranted by viewModel.isNotificationGranted.collectAsState()
                val isAdminGranted by viewModel.isAdminGranted.collectAsState()

                if (!isAccessibilityGranted || !isNotificationGranted || !isAdminGranted) {
                    Dialog(onDismissRequest = {
                        // block
                    }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(
                                text = "Необходимы следующие разрешения:",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                textAlign = TextAlign.Center,
                            )

                            if (!isAccessibilityGranted) {
                                OutlinedButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    onClick = {
                                        val openSettings =
                                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        openSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
                                        startActivity(openSettings)
                                    },
                                ) {
                                    Text(text = "Специальные возможности")
                                }
                            }

                            if (!isNotificationGranted) {
                                OutlinedButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            ActivityCompat.requestPermissions(
                                                this@MainActivity,
                                                arrayOf(POST_NOTIFICATIONS),
                                                1,
                                            )
                                        }
                                    },
                                ) {
                                    Text(text = "Уведомления")
                                }
                            }

                            if (!isAdminGranted) {
                                OutlinedButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    onClick = {
                                        val adminAskIntent = Intent(ACTION_ADD_DEVICE_ADMIN).apply {
                                            putExtra(EXTRA_DEVICE_ADMIN, adminReceiver)
                                            putExtra(
                                                EXTRA_ADD_EXPLANATION,
                                                "Не против, если я позаимствую немного админских прав?",
                                            )
                                        }
                                        startActivity(adminAskIntent)
                                    },
                                ) {
                                    Text(text = "Права админа")
                                }
                            }

                        }
                    }
                }

                Screen(
                    applicationList = appInfoList,
                    onSwitchClick = { packageId, checked ->
                        viewModel.onSwitchClick(packageId, checked)

                        // Нужно, чтобы перерисовать экран после изменений
                        val appListInfoWithChanges = appInfoList
                        val indexAppToChange =
                            appListInfoWithChanges.indexOfFirst { it.packageId == packageId }
                        val appToChange = appListInfoWithChanges[indexAppToChange]
                        appListInfoWithChanges[indexAppToChange] = appToChange.copy(
                            isSecured = checked,
                        )

                        appInfoList = appListInfoWithChanges
                    },
                )
            }
        }
    }

    @Composable
    private fun Screen(
        applicationList: List<ApplicationInfo>,
        onSwitchClick: (String, Boolean) -> Unit,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
        ) {
            item(
                key = "change_pin_text",
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        openConfirmActivityWithSettingPinCode()
                    },
                ) {
                    Text(
                        color = Color.Black,
                        text = "Change PIN",
                    )
                }
            }
            items(
                items = applicationList,
                key = { appInfo ->
                    appInfo.packageId
                },
            ) { info ->
                AppRow(appInfo = info, onSwitchClick = onSwitchClick)
            }
        }
    }

    @Composable
    private fun AppRow(appInfo: ApplicationInfo, onSwitchClick: (String, Boolean) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color.DarkGray,
                    uncheckedTrackColor = Color.LightGray,
                ),
                checked = appInfo.isSecured,
                onCheckedChange = {
                    onSwitchClick(appInfo.packageId, it)
                },
            )
            Image(
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp),
                painter = rememberAsyncImagePainter(model = appInfo.icon),
                contentDescription = appInfo.name,
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 16.sp,
                text = appInfo.name,
                maxLines = 1,
            )
        }
    }

    /**
     * Проверка необходимых разрешений:
     * 1. Специальные возможности (AccessibilityService)
     * 2. Уведомления
     * 3. Права админа
     */
    private fun checkPermission() {
        val isAccessibilityGranted = isAccessibilityServiceEnabled()
        viewModel.setAccessibilityPermission(isAccessibilityGranted)

        val isAdminGranted = dpm.isAdminActive(adminReceiver)
        viewModel.setAdminPermission(isAdminGranted)

        val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        viewModel.setNotificationPermission(isNotificationGranted)
    }

    private fun openConfirmActivityWithSettingPinCode() {
        val startActivityIntent = Intent(applicationContext, ConfirmActivity::class.java)
            .apply {
                setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION
                            or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
                )
                putExtra("isSettingPinCode", true)
            }
        startActivity(startActivityIntent)
    }

    data class ApplicationInfo(
        val icon: Bitmap,
        val name: String,
        val packageId: String,
        val isSecured: Boolean,
    )

    companion object {
        private const val APP_PACKAGE_ID = "com.dradefire.securepinonapp"
    }
}
