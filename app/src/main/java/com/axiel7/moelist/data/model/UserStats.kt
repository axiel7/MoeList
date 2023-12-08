package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStats(
    val anime: AnimeStats,
    val manga: MangaStats,
) {

    @Serializable
    data class AnimeStats(
        @SerialName("days_watched")
        override val days: Float,
        @SerialName("mean_score")
        override val meanScore: Float,
        @SerialName("watching")
        override val current: Int,
        @SerialName("completed")
        override val completed: Int,
        @SerialName("on_hold")
        override val onHold: Int,
        @SerialName("dropped")
        override val dropped: Int,
        @SerialName("plan_to_watch")
        override val planned: Int,
        @SerialName("total_entries")
        override val totalEntries: Int,
        @SerialName("rewatched")
        override val repeat: Int,
        @SerialName("episodes_watched")
        val episodesWatched: Int,
    ) : MediaStats

    @Serializable
    data class MangaStats(
        @SerialName("days_read")
        override val days: Float,
        @SerialName("mean_score")
        override val meanScore: Float,
        @SerialName("reading")
        override val current: Int,
        @SerialName("completed")
        override val completed: Int,
        @SerialName("on_hold")
        override val onHold: Int,
        @SerialName("dropped")
        override val dropped: Int,
        @SerialName("plan_to_read")
        override val planned: Int,
        @SerialName("total_entries")
        override val totalEntries: Int,
        @SerialName("reread")
        override val repeat: Int,
        @SerialName("chapters_read")
        val chaptersRead: Int,
        @SerialName("volumes_read")
        val volumesRead: Int,
    ) : MediaStats

    interface MediaStats {
        val days: Float
        val meanScore: Float
        val current: Int
        val completed: Int
        val onHold: Int
        val dropped: Int
        val planned: Int
        val totalEntries: Int
        val repeat: Int
    }
}
