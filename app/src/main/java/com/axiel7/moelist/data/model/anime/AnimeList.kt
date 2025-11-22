package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Stable
import com.axiel7.moelist.data.model.media.BaseMediaList
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class AnimeList(
    override val node: AnimeNode
) : BaseMediaList
