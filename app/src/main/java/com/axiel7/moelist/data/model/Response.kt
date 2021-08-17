package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("paging")
    val paging: Paging? = null,
    @SerialName("error")
    val error: String? = null,
    @SerialName("message")
    val message: String? = null
)
