package com.axiel7.moelist.data.model

import com.axiel7.moelist.data.model.anime.AnimeNode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Related(
    @SerialName("node")
    val node: AnimeNode,
    @SerialName("relation_type")
    val relationType: String = "",
    @SerialName("relation_type_formatted")
    val relationTypeFormatted: String = "",
)