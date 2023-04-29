package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName

abstract class BaseResponse {
    @SerialName("error")
    abstract val error: String?
    @SerialName("message")
    abstract val message: String?
}