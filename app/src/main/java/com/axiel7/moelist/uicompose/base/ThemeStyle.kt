package com.axiel7.moelist.uicompose.base

import com.axiel7.moelist.R

enum class ThemeStyle {
    FOLLOW_SYSTEM, LIGHT, DARK, BLACK;

    val stringRes
        get() = when (this) {
            FOLLOW_SYSTEM -> R.string.theme_system
            LIGHT -> R.string.theme_light
            DARK -> R.string.theme_dark
            BLACK -> R.string.theme_black
        }

    companion object {
        fun valueOfOrNull(value: String) = try {
            valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }

        val entriesLocalized = entries.associateWith { it.stringRes }
    }
}