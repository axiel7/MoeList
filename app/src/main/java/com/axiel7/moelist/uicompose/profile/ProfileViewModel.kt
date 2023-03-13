package com.axiel7.moelist.uicompose.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : BaseViewModel() {

    var user by mutableStateOf<User?>(null)

    fun getMyUser() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            user = UserRepository.getMyUser()
            isLoading = false
        }
    }
}