package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MangaNode

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