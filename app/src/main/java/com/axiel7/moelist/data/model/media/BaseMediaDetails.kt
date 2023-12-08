package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.BaseResponse
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.anime.RelatedAnime
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.RelatedManga
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull

abstract class BaseMediaDetails : BaseResponse {
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
    abstract val mediaType: MediaFormat?
    abstract val status: MediaStatus?
    abstract val genres: List<Genre>?
    abstract val pictures: List<MainPicture>?
    abstract val background: String?
    abstract val relatedAnime: List<RelatedAnime>?
    abstract val relatedManga: List<RelatedManga>?
    abstract val recommendations: List<Recommendations<*>>?

    abstract val myListStatus: BaseMyListStatus?

    override val error: String? = null
    override val message: String? = null

    fun userPreferredTitle() = title(App.titleLanguage)

    fun title(language: TitleLanguage) = when (language) {
        TitleLanguage.ROMAJI -> title
        TitleLanguage.ENGLISH ->
            if (alternativeTitles?.en.isNullOrBlank()) title
            else alternativeTitles?.en ?: title

        TitleLanguage.JAPANESE ->
            if (alternativeTitles?.ja.isNullOrBlank()) title
            else alternativeTitles?.ja ?: title
    }

    @Composable
    fun durationText() = when (this) {
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

    @Composable
    fun rankText() = if (rank == null) "N/A" else "#$rank"

    @Composable
    fun synonymsJoined(): String? {
        val joined = alternativeTitles?.synonyms?.joinToString(",\n")
        return if (joined?.isNotBlank() == true) joined
        else null
    }

    @Composable
    fun synopsisAndBackground() = buildAnnotatedString {
        val hasSynopsis = !synopsis.isNullOrBlank()
        if (hasSynopsis) append(synopsis)
        if (!background.isNullOrBlank()) {
            if (hasSynopsis) append("\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.synopsis_background))
            }
            append("\n$background")
        }
    }
}