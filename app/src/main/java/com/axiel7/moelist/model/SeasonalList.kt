package com.axiel7.moelist.model

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "seasonal_list", primaryKeys = ["node"],
    indices = [Index(value = ["node"], unique = true)])
data class SeasonalList(
    val node: NodeSeasonal
)

