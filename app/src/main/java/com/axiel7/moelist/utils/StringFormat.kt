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
    fun formatListStatus(status: String?): String {
        var result = ""
        when (status) {
            "watching" -> result = "Watching"
            "completed" -> result = "Completed"
            "on_hold" -> result = "On Hold"
            "dropped" -> result = "Dropped"
            "plan_to_watch" -> result = "Plan to Watch"
        }
        return result
    }
    fun formatListStatusInverted(status: String?): String {
        var result = ""
        when (status) {
            "Watching" -> result = "watching"
            "Completed" -> result = "completed"
            "On Hold" -> result = "on_hold"
            "Dropped" -> result = "dropped"
            "Plan to Watch" -> result = "plan_to_watch"
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
    fun formatScore(score: Int): String {
        var result = ""
        when (score) {
            0 -> result = "─"
            1 -> result = "1 Appalling"
            2 -> result = "2 Horrible"
            3 -> result = "3 Very Bad"
            4 -> result = "4 Bad"
            5 -> result = "5 Average"
            6 -> result = "6 Fine"
            7 -> result = "7 Good"
            8 -> result = "8 Very Good"
            9 -> result = "9 Great"
            10 -> result = "10 Masterpiece"
        }
        return result
    }
}