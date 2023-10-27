package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RankingType : Localizable {
    @SerialName("all")
    SCORE,

    @SerialName("bypopularity")
    POPULARITY,

    @SerialName("favorite")
    FAVORITE,

    @SerialName("upcoming")
    UPCOMING,

    @SerialName("airing")
    AIRING;

    @Composable
    override fun localized() = stringResource(stringRes)

    val stringRes
        get() = when (this) {
            SCORE -> R.string.sort_score
            POPULARITY -> R.string.popularity
            FAVORITE -> R.string.favorite
            UPCOMING -> R.string.upcoming
            AIRING -> R.string.airing
        }

    val serialName
        get() = when (this) {
            SCORE -> "all"
            POPULARITY -> "bypopularity"
            FAVORITE -> "favorite"
            UPCOMING -> "upcoming"
            AIRING -> "airing"
        }

    companion object {

        val rankingAnimeValues = arrayOf(SCORE, POPULARITY, FAVORITE, UPCOMING)

        val rankingMangaValues = arrayOf(SCORE, POPULARITY, FAVORITE)
    }
}