package com.axiel7.moelist.uicompose.base

import com.axiel7.moelist.R

enum class AppLanguage(val value: String) {
    FOLLOW_SYSTEM("follow_system"),
    ENGLISH("en"),
    ARABIC("ar-SA"),
    BULGARIAN("bg-BG"),
    CHINESE_SIMPLIFIED("zh-Hans"),
    CHINESE_TRADITIONAL("zh-Hant"),
    CZECH("cs"),
    FRENCH("fr"),
    GERMAN("de"),
    INDONESIAN("in-ID"),
    JAPANESE("ja"),
    PORTUGUESE("pt-PT"),
    PORTUGUESE_BRAZILIAN("pt-BR"),
    RUSSIAN("ru-RU"),
    SLOVAK("sk"),
    SPANISH("es"),
    TURKISH("tr"),
    UKRAINIAN("uk-UA");

    val stringResNative
        get() = when (this) {
            FOLLOW_SYSTEM -> R.string.theme_system
            ENGLISH -> R.string.english_native
            ARABIC -> R.string.arabic_native
            BULGARIAN -> R.string.bulgarian_native
            CHINESE_SIMPLIFIED -> R.string.chinese_simplified_native
            CHINESE_TRADITIONAL -> R.string.chinese_traditional_native
            CZECH -> R.string.czech_native
            FRENCH -> R.string.french_native
            GERMAN -> R.string.german_native
            INDONESIAN -> R.string.indonesian_native
            JAPANESE -> R.string.japanese_native
            PORTUGUESE -> R.string.portuguese_native
            PORTUGUESE_BRAZILIAN -> R.string.brazilian_native
            RUSSIAN -> R.string.russian_native
            SLOVAK -> R.string.slovak_native
            SPANISH -> R.string.spanish_native
            TURKISH -> R.string.turkish_native
            UKRAINIAN -> R.string.ukrainian_native
        }

    companion object {
        fun valueOf(isoTag: String) = entries.find { it.value == isoTag }

        val entriesLocalized = entries.associateWith { it.stringResNative }
    }
}