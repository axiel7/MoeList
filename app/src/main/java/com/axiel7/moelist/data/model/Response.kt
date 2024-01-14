package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("paging")
    val paging: Paging? = null,
    override val error: String? = null,
    override val message: String? = null,
) : BaseResponse {
    val wasError = data == null || error != null || message != null
    val isSuccess = !wasError
}
