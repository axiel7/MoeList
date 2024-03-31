package com.axiel7.moelist.ui.more.settings

import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.AppLanguage
import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.event.UiEvent

interface SettingsEvent : UiEvent {
    fun setLanguage(value: AppLanguage)
    fun setTheme(value: ThemeStyle)
    fun setUseBlackColors(value: Boolean)
    fun setShowNsfw(value: Boolean)
    fun setUseGeneralListStyle(value: Boolean)
    fun setGeneralListStyle(value: ListStyle)
    fun setItemsPerRow(value: ItemsPerRow)
    fun setStartTab(value: StartTab)
    fun setTitleLanguage(value: TitleLanguage)
    fun setUseListTabs(value: Boolean)
    fun setLoadCharacters(value: Boolean)
    fun setRandomListEntryEnabled(value: Boolean)
}