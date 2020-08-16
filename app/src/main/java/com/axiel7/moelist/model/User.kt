package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "user", primaryKeys = ["id"],
    indices = [Index(value = ["id"], unique = true)])
data class User(
    val id: Int,
    val name: String?,
    val gender: String?,
    val birthday: String?,
    val location: String?,
    val joined_at: String?,
    val picture: String?,
    val anime_statistics: UserAnimeStatistics
)