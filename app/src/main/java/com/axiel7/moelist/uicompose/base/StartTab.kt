package com.axiel7.moelist.uicompose.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable

enum class StartTab(
    val value: String
) : Localizable {
    LAST_USED("last_used"),
    HOME("home"),
    ANIME("anime"),
    MANGA("manga"),
    MORE("more");

    val stringRes
        get() = when (this) {
            LAST_USED -> R.string.last_used
            HOME -> R.string.title_home
            ANIME -> R.string.title_anime_list
            MANGA -> R.string.title_manga_list
            MORE -> R.string.more
        }

    @Composable
    override fun localized() = stringResource(stringRes)

    companion object {
        fun valueOf(tabName: String) = entries.find { it.value == tabName }

        val entriesLocalized = entries.associateWith { it.stringRes }
    }
}