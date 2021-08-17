package com.axiel7.moelist.data.model.manga

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthorNode(
    @SerialName("id")
    val id: Int,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String
)