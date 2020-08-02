package com.axiel7.moelist.model

data class AccessToken(
    val token_type: String,
    val expires_in: Int,
    val access_token: String,
    val refresh_token: String
)