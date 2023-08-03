package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSeason(
    @SerialName("year")
    val year: Int,
    @SerialName("season")
    val season: Season
) {

    @Composable
    fun seasonYearText() = "${season.localized()} $year"
}