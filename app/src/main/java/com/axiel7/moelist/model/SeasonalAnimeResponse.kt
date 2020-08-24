package com.axiel7.moelist.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "seasonal_response", primaryKeys = ["data"],
    indices = [Index(value = ["data"], unique = true)])
data class SeasonalAnimeResponse(
    val data: MutableList<SeasonalList>,
    @Embedded val paging: Paging?,
    @Embedded val season: StartSeason?
)