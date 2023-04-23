package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.utils.Extensions.toStringPositiveValueOrNull

abstract class BaseMediaList {
    abstract val node: BaseMediaNode
}

@Composable
fun BaseMediaList.durationText() = when (this) {
    is AnimeList -> {
        val stringValue = this.node.numEpisodes.toStringPositiveValueOrNull()
        if (stringValue == null) "??"
        else "$stringValue ${stringResource(R.string.episodes)}"
    }
    is MangaList -> {
        val stringValue = this.node.numChapters.toStringPositiveValueOrNull()
        if (stringValue == null) "??"
        else "$stringValue ${stringResource(R.string.chapters)}"
    }

    else -> "??"
}