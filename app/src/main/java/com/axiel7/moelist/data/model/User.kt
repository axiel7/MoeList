package com.axiel7.moelist.data.model

import com.axiel7.moelist.data.model.anime.UserAnimeStatistics
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("birthday")
    val birthday: String? = null,
    @SerialName("location")
    val location: String? = null,
    @SerialName("joined_at")
    val joinedAt: String? = null,
    @SerialName("picture")
    val picture: String? = null,
    @SerialName("anime_statistics")
    val animeStatistics: UserAnimeStatistics? = null,

    override var message: String? = null,
    override var error: String? = null,
) : BaseResponse()