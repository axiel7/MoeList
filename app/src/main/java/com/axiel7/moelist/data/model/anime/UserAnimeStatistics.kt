package com.axiel7.moelist.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAnimeStatistics(
    @SerialName("num_items_watching")
    val numItemsWatching: Int?,
    @SerialName("num_items_completed")
    val numItemsCompleted: Int?,
    @SerialName("num_items_on_hold")
    val numItemsOnHold: Int?,
    @SerialName("num_items_dropped")
    val numItemsDropped: Int?,
    @SerialName("num_items_plan_to_watch")
    val numItemsPlanToWatch: Int?,
    @SerialName("num_items")
    val numItems: Int?,
    @SerialName("num_days_watched")
    val numDaysWatched: Float?,
    @SerialName("num_days_watching")
    val numDaysWatching: Float?,
    @SerialName("num_days_completed")
    val numDaysCompleted: Float?,
    @SerialName("num_days_on_hold")
    val numDaysOnHold: Float?,
    @SerialName("num_days_dropped")
    val numDaysDropped: Float?,
    @SerialName("num_days")
    val numDays: Float?,
    @SerialName("num_episodes")
    val numEpisodes: Int?,
    @SerialName("num_times_rewatched")
    val numTimesRewatched: Int?,
    @SerialName("mean_score")
    val meanScore: Float?,
)