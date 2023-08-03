package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable

enum class MediaSort(val value: String) : Localizable {
    ANIME_TITLE("anime_title"),
    ANIME_SCORE("anime_score"),
    ANIME_NUM_USERS("anime_num_list_users"),
    ANIME_START_DATE("anime_start_date"),
    SCORE("list_score"),
    UPDATED("list_updated_at"),
    MANGA_TITLE("manga_title"),
    MANGA_START_DATE("manga_start_date");

    override fun toString() = value

    @Composable
    override fun localized() = when (this) {
        ANIME_TITLE -> stringResource(R.string.sort_title)
        ANIME_SCORE -> stringResource(R.string.sort_score)
        ANIME_NUM_USERS -> stringResource(R.string.members)
        ANIME_START_DATE -> stringResource(R.string.sort_start_date)
        SCORE -> stringResource(R.string.sort_score)
        UPDATED -> stringResource(R.string.sort_last_updated)
        MANGA_TITLE -> stringResource(R.string.sort_title)
        MANGA_START_DATE -> stringResource(R.string.sort_start_date)
    }

    companion object {
        fun forValue(value: String) = values().firstOrNull { it.value == value }

        val animeListSortItems = arrayOf(ANIME_TITLE, SCORE, UPDATED, ANIME_START_DATE)

        val mangaListSortItems = arrayOf(MANGA_TITLE, SCORE, UPDATED, MANGA_START_DATE)
    }
}