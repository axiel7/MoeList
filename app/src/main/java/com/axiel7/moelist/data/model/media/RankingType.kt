package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import kotlinx.serialization.Serializable

@Serializable
enum class RankingType(val value: String) {
    SCORE("all"),
    POPULARITY("bypopularity"),
    FAVORITE("favorite"),
    UPCOMING("upcoming")
}

fun rankingAnimeValues() =
    arrayOf(RankingType.SCORE, RankingType.POPULARITY, RankingType.FAVORITE, RankingType.UPCOMING)

fun rankingMangaValues() =
    arrayOf(RankingType.SCORE, RankingType.POPULARITY, RankingType.FAVORITE)

@Composable
fun RankingType.localized() = when (this) {
    RankingType.SCORE -> stringResource(R.string.sort_score)
    RankingType.POPULARITY -> stringResource(R.string.popularity)
    RankingType.FAVORITE -> stringResource(R.string.favorite)
    RankingType.UPCOMING -> stringResource(R.string.upcoming)
}