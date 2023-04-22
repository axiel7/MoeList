package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.BaseResponse
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.utils.Extensions.toStringPositiveValueOrNull

abstract class BaseMediaDetails : BaseResponse() {
    abstract val id: Int
    abstract val title: String?
    abstract val mainPicture: MainPicture?
    abstract val alternativeTitles: AlternativeTitles?
    abstract val startDate: String?
    abstract val endDate: String?
    abstract val synopsis: String?
    abstract val mean: Float?
    abstract val rank: Int?
    abstract val popularity: Int?
    abstract val numListUsers: Int?
    abstract val numScoringUsers: Int?
    abstract val nsfw: String?
    abstract val createdAt: String?
    abstract val updatedAt: String?
    abstract val mediaType: String?
    abstract val status: String?
    abstract val genres: List<Genre>?
    abstract val pictures: List<MainPicture>?
    abstract val background: String?
    abstract val relatedAnime: List<Related>?
    abstract val relatedManga: List<Related>?
    abstract val recommendations: List<Recommendations>?

    abstract val myListStatus: BaseMyListStatus?

    override var error: String? = null
    override var message: String? = null
}

@Composable
fun BaseMediaDetails.durationText() = when (this) {
    is AnimeDetails -> {
        val stringValue = numEpisodes.toStringPositiveValueOrNull()
        if (stringValue == null) stringResource(R.string.unknown)
        else "$stringValue ${stringResource(R.string.episodes)}"
    }
    is MangaDetails -> {
        val stringValue = numChapters.toStringPositiveValueOrNull()
        if (stringValue == null) stringResource(R.string.unknown)
        else "$stringValue ${stringResource(R.string.chapters)}"
    }
    else -> stringResource(R.string.unknown)
}

/**
 * @return the total num of episodes or chapters
 */
fun BaseMediaDetails.totalDuration() = when (this) {
    is AnimeDetails -> numEpisodes
    is MangaDetails -> numChapters
    else -> null
}

@Composable
fun String.mediaFormatLocalized() = when (this) {
    "tv" -> stringResource(R.string.tv)
    "ova" -> stringResource(R.string.ova)
    "ona" -> stringResource(R.string.ona)
    "movie" -> stringResource(R.string.movie)
    "special" -> stringResource(R.string.special)
    "music" -> stringResource(R.string.music)
    "unknown" -> stringResource(R.string.unknown)
    "manga" -> stringResource(R.string.manga)
    "one_shot" -> stringResource(R.string.one_shot)
    "manhwa" -> stringResource(R.string.manhwa)
    "manhua" -> stringResource(R.string.manhua)
    "novel" -> stringResource(R.string.novel)
    "light_novel" -> stringResource(R.string.light_novel)
    "doujinshi" -> stringResource(R.string.doujinshi)
    else -> this
}

@Composable
fun String.statusLocalized() = when (this) {
    "currently_airing" -> stringResource(R.string.airing)
    "finished_airing" -> stringResource(R.string.finished)
    "not_yet_aired" -> stringResource(R.string.not_yet_aired)
    "currently_publishing" -> stringResource(R.string.publishing)
    "finished" -> stringResource(R.string.finished)
    "on_hiatus" -> stringResource(R.string.on_hiatus)
    "discontinued" -> stringResource(R.string.discontinued)
    else -> this
}

@Composable
fun BaseMediaDetails.rankText() = if (rank == null) "N/A" else "#$rank"

@Composable
fun BaseMediaDetails.synonymsJoined() = this.alternativeTitles?.synonyms?.joinToString(",\n")
