package com.axiel7.moelist.ui.base.navigation

import com.axiel7.moelist.data.model.media.MediaType
import kotlinx.serialization.Serializable

sealed interface Route {
    sealed interface Tab : Route {
        @Serializable
        data object Home : Tab

        @Serializable
        data class Anime(val mediaType: MediaType) : Tab

        @Serializable
        data class Manga(val mediaType: MediaType) : Tab

        @Serializable
        data object More : Tab
    }

    @Serializable
    data class MediaRanking(val mediaType: MediaType) : Route

    @Serializable
    data class MediaDetails(
        val mediaType: MediaType,
        val mediaId: Int,
    ) : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object SeasonChart : Route

    @Serializable
    data object Recommendations : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object Search : Route

    @Serializable
    data class FullPoster(val pictures: List<String>) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object ListStyleSettings : Route

    @Serializable
    data object Notifications : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Credits : Route
}

