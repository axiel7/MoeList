package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseRelated
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RelatedManga(
    @SerialName("node")
    override val node: MangaNode,
    @SerialName("relation_type")
    override val relationType: String,
    @SerialName("relation_type_formatted")
    override val relationTypeFormatted: String
): BaseRelated()
