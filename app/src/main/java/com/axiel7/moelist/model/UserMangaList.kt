package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "user_manga_list", primaryKeys = ["node"],
    indices = [Index(value = ["node"], unique = true)])
data class UserMangaList(
    val node: NodeManga,
    val list_status: MyMangaListStatus?,
    var status: String?
)