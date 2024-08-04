package com.dradefire.securepinonapp.ui.main

import androidx.lifecycle.ViewModel
import com.dradefire.securepinonapp.repository.SharedPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
) : ViewModel() {
    private val _isAccessibilityGranted = MutableStateFlow(false)
    val isAccessibilityGranted = _isAccessibilityGranted.asStateFlow()

    private val _isAdminGranted = MutableStateFlow(false)
    val isAdminGranted = _isAdminGranted.asStateFlow()

    private val _isNotificationGranted = MutableStateFlow(false)
    val isNotificationGranted = _isNotificationGranted.asStateFlow()

    fun setAccessibilityPermission(isGranted: Boolean) {
        _isAccessibilityGranted.update { isGranted }
    }

    fun setAdminPermission(isGranted: Boolean) {
        _isAdminGranted.update { isGranted }
    }

    fun setNotificationPermission(isGranted: Boolean) {
        _isNotificationGranted.update { isGranted }
    }

    fun onSwitchClick(packageId: String, checked: Boolean) {
        val packageIdList = sharedPreferencesRepository.packageIdList.toMutableSet()

        if (checked) {
            packageIdList.add(packageId)
        } else {
            packageIdList.remove(packageId)
        }

        sharedPreferencesRepository.packageIdList = packageIdList.toList()
    }
}