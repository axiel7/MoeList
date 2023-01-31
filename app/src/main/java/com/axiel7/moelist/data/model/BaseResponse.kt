package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName

abstract class BaseResponse {
    @SerialName("error")
    abstract var error: String?
    @SerialName("message")
    abstract var message: String?
}