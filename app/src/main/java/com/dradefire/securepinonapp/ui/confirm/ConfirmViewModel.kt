package com.dradefire.securepinonapp.ui.confirm

import androidx.lifecycle.ViewModel
import com.dradefire.securepinonapp.repository.SharedPreferencesRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfirmViewModel(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
) : ViewModel() {
    private val correctPinCode = sharedPreferencesRepository.pinCode
    val isPinCodeNotExist = sharedPreferencesRepository.pinCode.isNullOrEmpty()

    private val _closeActivityEvent = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val closeActivityEvent = _closeActivityEvent.asSharedFlow()

    private val _pinCode = MutableStateFlow("")
    val pinCode = _pinCode.asStateFlow()
    val isPinValid
        get() = _pinCode.value == correctPinCode

    fun onButtonClick(event: ButtonClickEvent, isSettingPinCode: Boolean) {
        when (event) {
            is ButtonClickEvent.Number -> {
                _pinCode.update { it + event.number }
                if (_pinCode.value.length >= 4) {
                    handleEnteredFullPinCode(isSettingPinCode)
                }
            }

            ButtonClickEvent.Delete -> {
                if (_pinCode.value.isNotEmpty()) {
                    _pinCode.update { it.substring(0, it.length - 1) }
                }
            }
        }
    }

    private fun handleEnteredFullPinCode(isSettingPinCode: Boolean) {
        if (isSettingPinCode) {
            sharedPreferencesRepository.pinCode = _pinCode.value
            onSuccessPinEnter()
        } else if (isPinValid) {
            onSuccessPinEnter()
        } else {
            _pinCode.update { "" }
        }
    }

    private fun onSuccessPinEnter() {
        sharedPreferencesRepository.isCorrectPin = true
        _closeActivityEvent.tryEmit(Unit)
    }

    sealed interface ButtonClickEvent {
        data class Number(val number: Int) : ButtonClickEvent
        data object Delete : ButtonClickEvent
    }
}