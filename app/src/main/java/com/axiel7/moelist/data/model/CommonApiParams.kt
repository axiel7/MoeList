package com.axiel7.moelist.data.model

data class CommonApiParams(
    var nsfw: Int? = 0,
    var fields: String? = "",
    var limit: Int = 50,
    var offset: Int? = 0,
)
