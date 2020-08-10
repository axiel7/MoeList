package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "user_anime_list", primaryKeys = ["node"],
    indices = [Index(value = ["node"], unique = true)])
data class UserAnimeList(
    val node: Node,
    val list_status: MyListStatus?,
    var status: String?
)