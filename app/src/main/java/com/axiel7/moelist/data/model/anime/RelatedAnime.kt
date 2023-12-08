package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.BaseRelated
import com.axiel7.moelist.data.model.media.RelationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RelatedAnime(
    @SerialName("node")
    override val node: AnimeNode,
    @SerialName("relation_type")
    override val relationType: RelationType,
) : BaseRelated
