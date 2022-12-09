package com.axiel7.moelist.data.model

data class ApiParams(
    var q: String? = "",
    var sort: String? = "",
    var status: String? = null,
    var nsfw: Int? = 0,
    var fields: String? = "",
    var limit: Int = 50,
    var offset: Int? = 0,
    var resetPage: Boolean = false,
)
