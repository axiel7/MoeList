package com.axiel7.moelist.model

data class StatisticsStatus(
    val watching: String,
    val completed: String,
    val on_hold: String,
    val dropped: String,
    val plan_to_watch: String
)