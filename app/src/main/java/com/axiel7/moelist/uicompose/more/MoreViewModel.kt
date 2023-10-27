package com.axiel7.moelist.uicompose.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.LoginRepository
import kotlinx.coroutines.launch

class MoreViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {
    fun logOut() = viewModelScope.launch {
        loginRepository.logOut()
    }
}