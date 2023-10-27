package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.R

enum class TitleLanguage {
    ROMAJI, ENGLISH, JAPANESE;

    val stringRes
        get() = when (this) {
            ROMAJI -> R.string.romaji
            ENGLISH -> R.string.english
            JAPANESE -> R.string.japanese
        }

    companion object {
        val entriesLocalized = entries.associateWith { it.stringRes }
    }
}