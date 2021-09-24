package com.axiel7.moelist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.axiel7.moelist.data.model.anime.UserAnimeStatistics
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity @Serializable
data class User(
    @PrimaryKey
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

    @SerialName("message")
    val message: String? = null,
    @SerialName("error")
    val error: String? = null,
)