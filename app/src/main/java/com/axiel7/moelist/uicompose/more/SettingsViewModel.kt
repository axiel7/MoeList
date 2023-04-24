package com.axiel7.moelist.uicompose.more

import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.base.BaseViewModel

class SettingsViewModel: BaseViewModel() {

    val themeEntries = mapOf(
        "light" to R.string.theme_light,
        "dark" to R.string.theme_dark,
        "follow_system" to R.string.theme_system
    )

    val sectionEntries = mapOf(
        "home" to R.string.title_home,
        "anime" to R.string.title_anime_list,
        "manga" to R.string.title_manga_list
    )

    val languageEntries = mapOf(
        "follow_system" to R.string.theme_system,
        "en" to R.string.english_native,
        "ar-rSA" to R.string.arabic_native,
        "bg-rBG" to R.string.bulgarian_native,
        "de" to R.string.german_native,
        "es" to R.string.spanish_native,
        "fr" to R.string.french_native,
        "pt-rBR" to R.string.brazilian_native,
        "ru-rRU" to R.string.russian_native,
        "tr" to R.string.turkish_native,
        "uk-rUA" to R.string.ukrainian_native,
        "ja" to R.string.japanese_native,
        "zh-Hant" to R.string.chinese_traditional_native,
        "zh-Hans" to R.string.chinese_simplified_native,
    )
}