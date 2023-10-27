package com.axiel7.moelist.uicompose.more.settings

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.uicompose.base.AppLanguage
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.uicompose.base.ItemsPerRow
import com.axiel7.moelist.uicompose.base.ListStyle
import com.axiel7.moelist.uicompose.base.StartTab
import com.axiel7.moelist.uicompose.base.ThemeStyle
import com.axiel7.moelist.utils.ContextExtensions.changeLocale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel() {

    val lang = defaultPreferencesRepository.lang
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(FLOW_TIMEOUT),
            AppLanguage.FOLLOW_SYSTEM
        )

    fun setLang(value: AppLanguage) = viewModelScope.launch {
        defaultPreferencesRepository.setLang(value)
        changeLocale(value.value)
        if (value == AppLanguage.JAPANESE) {
            setTitleLang(TitleLanguage.JAPANESE)
        }
    }

    val theme = defaultPreferencesRepository.theme
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(FLOW_TIMEOUT),
            ThemeStyle.FOLLOW_SYSTEM
        )

    fun setTheme(value: ThemeStyle) = viewModelScope.launch {
        defaultPreferencesRepository.setTheme(value)
    }

    val nsfw = defaultPreferencesRepository.nsfw
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), false)

    fun setNsfw(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setNsfw(value)
    }

    val useGeneralListStyle = defaultPreferencesRepository.useGeneralListStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), true)

    fun setUseGeneralListStyle(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setUseGeneralListStyle(value)
    }

    val generalListStyle = defaultPreferencesRepository.generalListStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), ListStyle.STANDARD)

    fun setGeneralListStyle(value: ListStyle) = viewModelScope.launch {
        defaultPreferencesRepository.setGeneralListStyle(value)
    }

    val itemsPerRow = defaultPreferencesRepository.gridItemsPerRow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), ItemsPerRow.DEFAULT)

    fun setItemsPerRow(value: ItemsPerRow) = viewModelScope.launch {
        defaultPreferencesRepository.setGridItemsPerRow(value)
    }

    val startTab = defaultPreferencesRepository.startTab
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), StartTab.LAST_USED)

    fun setStartTab(value: StartTab) = viewModelScope.launch {
        defaultPreferencesRepository.setStartTab(value)
    }

    val titleLang = defaultPreferencesRepository.titleLang
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), TitleLanguage.ROMAJI)

    fun setTitleLang(value: TitleLanguage) = viewModelScope.launch {
        defaultPreferencesRepository.setTitleLang(value)
    }

    val useListTabs = defaultPreferencesRepository.useListTabs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), false)

    fun setUseListTabs(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setUseListTabs(value)
    }

    val loadCharacters = defaultPreferencesRepository.loadCharacters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), false)

    fun setLoadCharacters(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setLoadCharacters(value)
    }

    val randomListEntryEnabled = defaultPreferencesRepository.randomListEntryEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), false)

    fun setRandomListEntryEnabled(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setRandomListEntryEnabled(value)
    }
}