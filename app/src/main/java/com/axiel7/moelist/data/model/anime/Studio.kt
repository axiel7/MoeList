package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Studio(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String
)