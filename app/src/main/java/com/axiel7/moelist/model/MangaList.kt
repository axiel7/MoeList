package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "manga_list", primaryKeys = ["node"],
    indices = [Index(value = ["node"], unique = true)])
data class MangaList(
    val node: NodeManga
)

