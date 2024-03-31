package com.axiel7.moelist.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.LoginRepository
import com.axiel7.moelist.ui.base.ThemeStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val loginRepository: LoginRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : ViewModel() {

    val titleLanguage = defaultPreferencesRepository.titleLang

    val startTab = defaultPreferencesRepository.startTab

    val lastTab = defaultPreferencesRepository.lastTab

    fun saveLastTab(value: Int) = viewModelScope.launch {
        defaultPreferencesRepository.setLastTab(value)
    }

    val theme = defaultPreferencesRepository.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeStyle.FOLLOW_SYSTEM)

    val useBlackColors = defaultPreferencesRepository.useBlackColors

    val accessToken = defaultPreferencesRepository.accessToken

    val useListTabs = defaultPreferencesRepository.useListTabs
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val profilePicture = defaultPreferencesRepository.profilePicture
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun parseIntentData(uri: Uri) = viewModelScope.launch {
        val code = uri.getQueryParameter("code")
        val receivedState = uri.getQueryParameter("state")
        if (code != null && receivedState == LoginRepository.STATE) {
            loginRepository.getAccessToken(code)
        }
    }
}
