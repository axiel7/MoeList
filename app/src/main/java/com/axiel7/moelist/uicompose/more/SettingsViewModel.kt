package com.axiel7.moelist.uicompose.more

import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.base.BaseViewModel

class SettingsViewModel: BaseViewModel() {

    val themeEntries = mapOf(
        "follow_system" to R.string.theme_system,
        "light" to R.string.theme_light,
        "dark" to R.string.theme_dark,
        "black" to R.string.theme_black
    )

    val languageEntries = mapOf(
        "follow_system" to R.string.theme_system,
        "en" to R.string.english_native,
        "ar-rSA" to R.string.arabic_native,
        "bg-rBG" to R.string.bulgarian_native,
        "cs-rCZ" to R.string.czech_native,
        "de" to R.string.german_native,
        "es" to R.string.spanish_native,
        "fr" to R.string.french_native,
        "in-rID" to R.string.indonesian_native,
        "pt-rPT" to R.string.portuguese_native,
        "pt-rBR" to R.string.brazilian_native,
        "ru-rRU" to R.string.russian_native,
        "tr" to R.string.turkish_native,
        "uk-rUA" to R.string.ukrainian_native,
        "ja" to R.string.japanese_native,
        "zh-Hant" to R.string.chinese_traditional_native,
        "zh-Hans" to R.string.chinese_simplified_native,
    )

    val startTabEntries = mapOf(
        "last_used" to R.string.last_used,
        "home" to R.string.title_home,
        "anime" to R.string.title_anime_list,
        "manga" to R.string.title_manga_list,
        "more" to R.string.more
    )

}