package com.axiel7.moelist.uicompose.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.axiel7.moelist.App
import com.axiel7.moelist.utils.Extensions.toInt

abstract class BaseViewModel: ViewModel() {
    var showMessage by mutableStateOf(false)
    var message by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    protected fun setErrorMessage(message: String) {
        showMessage = true
        this.message = message
    }

    //protected val sharedPrefs: SharedPrefsHelpers? by lazy { SharedPrefsHelpers.instance }
    /*protected val nsfw: Int by lazy {
        App.nsfw.toInt()
    }*/
    protected val nsfw = 0
}