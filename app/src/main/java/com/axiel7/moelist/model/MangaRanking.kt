package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "manga_ranking", primaryKeys = ["node"],
    indices = [Index(value = ["node"], unique = true)])
data class MangaRanking(
    val node: NodeManga,
    val ranking: Ranking?
)

