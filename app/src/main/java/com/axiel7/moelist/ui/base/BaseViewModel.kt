package com.axiel7.moelist.ui.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    var showMessage by mutableStateOf(false)
    var message by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    protected fun setErrorMessage(message: String) {
        showMessage = true
        this.message = message
    }

    companion object {
        const val GENERIC_ERROR = "Generic Error"
        const val FLOW_TIMEOUT = 5_000L
    }
}