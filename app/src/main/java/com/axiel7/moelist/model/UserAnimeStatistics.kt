package com.axiel7.moelist.model

data class UserAnimeStatistics(
    val num_items_watching: Int?,
    val num_items_completed: Int?,
    val num_items_on_hold: Int?,
    val num_items_dropped: Int?,
    val num_items_plan_to_watch: Int?,
    val num_items: Int?,
    val num_days_watched: Float?,
    val num_days_watching: Float?,
    val num_days_completed: Float?,
    val num_days_on_hold: Float?,
    val num_days_dropped: Float?,
    val num_days: Float?,
    val num_episodes: Int?,
    val num_times_rewatched: Int?,
    val mean_score: Float?
)