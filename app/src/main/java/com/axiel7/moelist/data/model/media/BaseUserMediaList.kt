package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.manga.isUsingVolumeProgress

abstract class BaseUserMediaList<T: BaseMediaNode> {
    abstract val node: T
    abstract val listStatus: BaseMyListStatus?
    abstract val status: String?
}

fun BaseUserMediaList<*>.userProgress() = when (this) {
    is UserAnimeList -> listStatus?.progress
    is UserMangaList -> {
        if (listStatus?.isUsingVolumeProgress() == true) {
            listStatus.numVolumesRead
        } else {
            listStatus?.progress
        }
    }
    else -> null
}

fun BaseUserMediaList<*>.totalProgress() = when (this) {
    is UserAnimeList -> node.numEpisodes
    is UserMangaList -> {
        if (listStatus?.isUsingVolumeProgress() == true) {
            node.numVolumes
        } else {
            node.numChapters
        }
    }
    else -> null
}

fun calculateProgressBarValue(
    currentProgress: Int?,
    totalProgress: Int?
): Float {
    val total = totalProgress ?: 0
    return if (total == 0) 0f
    else (currentProgress ?: 0).div(total.toFloat())
}