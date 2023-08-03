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
    override fun localized() = when (this) {
        SCORE -> stringResource(R.string.sort_score)
        POPULARITY -> stringResource(R.string.popularity)
        FAVORITE -> stringResource(R.string.favorite)
        UPCOMING -> stringResource(R.string.upcoming)
        AIRING -> stringResource(R.string.airing)
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