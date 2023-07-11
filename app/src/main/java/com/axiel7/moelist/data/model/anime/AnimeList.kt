package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.BaseMediaList
import kotlinx.serialization.Serializable

@Serializable
data class AnimeList(
    override val node: AnimeNode
) : BaseMediaList()
