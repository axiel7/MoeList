package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "anime_ranking", primaryKeys = ["node"],
    indices = [Index(value = ["node"], unique = true)])
data class AnimeRanking(
    val node: Node,
    val ranking: Ranking?
)

