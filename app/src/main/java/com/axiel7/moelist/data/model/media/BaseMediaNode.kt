package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull

abstract class BaseMediaNode {
    abstract val id: Int
    abstract val title: String
    abstract val mainPicture: MainPicture?
    abstract val numListUsers: Int?
    abstract val mediaType: String?
    abstract val status: String?
    abstract val mean: Float?
}

fun BaseMediaNode.totalDuration() = when (this) {
    is AnimeNode -> this.numEpisodes
    is MangaNode -> this.numChapters
    else -> null
}

@Composable
fun BaseMediaNode.durationText() = when (this) {
    is AnimeNode -> {
        val stringValue = this.numEpisodes.toStringPositiveValueOrNull()
        if (stringValue == null) "??"
        else "$stringValue ${stringResource(R.string.episodes)}"
    }
    is MangaNode -> {
        val stringValue = this.numChapters.toStringPositiveValueOrNull()
        if (stringValue == null) "??"
        else "$stringValue ${stringResource(R.string.chapters)}"
    }

    else -> "??"
}