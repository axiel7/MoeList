package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseRelated
import com.axiel7.moelist.data.model.media.RelationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RelatedManga(
    @SerialName("node")
    override val node: MangaNode,
    @SerialName("relation_type")
    override val relationType: RelationType,
) : BaseRelated()
