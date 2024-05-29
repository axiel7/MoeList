package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.NodeSeasonal
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.utils.UNKNOWN_CHAR

abstract class BaseMediaNode {
    abstract val id: Int
    abstract val title: String
    abstract val alternativeTitles: AlternativeTitles?
    abstract val mainPicture: MainPicture?
    open val numListUsers: Int? = null
    abstract val mediaFormat: MediaFormat?
    abstract val status: MediaStatus?
    abstract val mean: Float?

    val mediaType
        get() = if (this is MangaNode) MediaType.MANGA else MediaType.ANIME

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

    fun totalDuration() = when (this) {
        is AnimeNode -> this.numEpisodes
        is MangaNode -> this.numChapters
        is NodeSeasonal -> this.numEpisodes
        else -> null
    }

    @Composable
    fun durationText() = when (this) {
        is AnimeNode, is NodeSeasonal -> {
            val totalDuration = this.totalDuration()
            if (totalDuration != null && totalDuration > 0) {
                pluralStringResource(
                    id = R.plurals.num_episodes,
                    count = totalDuration,
                    totalDuration
                )
            } else UNKNOWN_CHAR
        }

        is MangaNode -> {
            if (numChapters != null && numChapters > 0) {
                pluralStringResource(
                    id = R.plurals.num_chapters,
                    count = numChapters,
                    numChapters
                )
            } else UNKNOWN_CHAR
        }

        else -> UNKNOWN_CHAR
    }
}