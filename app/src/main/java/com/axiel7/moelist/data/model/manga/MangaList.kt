package com.axiel7.moelist.data.model.manga

import androidx.compose.runtime.Stable
import com.axiel7.moelist.data.model.media.BaseMediaList
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class MangaList(
    override val node: MangaNode
) : BaseMediaList

