package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseMediaList
import kotlinx.serialization.Serializable

@Serializable
data class MangaList(
    override val node: MangaNode
) : BaseMediaList

