package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("paging")
    val paging: Paging? = null,
    override var error: String? = null,
    override var message: String? = null,
) : BaseResponse()
