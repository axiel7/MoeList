package com.axiel7.moelist.utils

object StringFormat {
    fun formatMediaType(mediaType: String): String {
        var result = ""
        when (mediaType) {
            "tv" -> result = "TV"
            "ova" -> result = "OVA"
            "ona" -> result = "ONA"
            "movie" -> result = "Movie"
            "special" -> result = "Special"
            "music" -> result = "Music"
            "unknown" -> result = "Unknown"
        }
        return result
    }
    fun formatStatus(status: String): String {
        var result = ""
        when (status) {
            "currently_airing" -> result = "Airing"
            "finished_airing" -> result = "Finished"
            "not_yet_aired" -> result = "Not Yet Aired"
        }
        return result
    }
    fun formatSeason(season: String): String {
        var result = ""
        when (season) {
            "winter" -> result = "Winter"
            "spring" -> result = "Spring"
            "summer" -> result = "Summer"
            "fall" -> result = "Fall"
        }
        return result
    }
    fun formatSource(source: String): String {
        var result = ""
        when (source) {
            "original" -> result = "Original"
            "manga" -> result = "Manga"
            "light_novel" -> result = "Light novel"
            "game" -> result = "Game"
        }
        return result
    }
}