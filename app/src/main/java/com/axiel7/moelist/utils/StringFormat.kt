package com.axiel7.moelist.utils

object StringFormat {
    fun formatMediaType(mediaType: String): String {
        var result = mediaType
        when (mediaType) {
            "tv" -> result = "TV"
            "ova" -> result = "OVA"
            "ona" -> result = "ONA"
            "movie" -> result = "Movie"
            "special" -> result = "Special"
            "music" -> result = "Music"
            "unknown" -> result = "Unknown"
            "manga" -> result = "Manga"
            "one_shot" -> result = "One Shot"
            "manhwa" -> result = "Manhwa"
            "novel" -> result = "Novel"
            "doujinshi" -> result = "Doujinshi"
        }
        return result
    }
    fun formatStatus(status: String): String {
        var result = status
        when (status) {
            "currently_airing" -> result = "Airing"
            "finished_airing" -> result = "Finished"
            "not_yet_aired" -> result = "Not Yet Aired"
            "currently_publishing" -> result = "Publishing"
            "finished" -> result = "Finished"
            "on_hiatus" -> result = "On Hiatus"
        }
        return result
    }
    fun formatListStatus(status: String?): String {
        var result = status ?: ""
        when (status) {
            "watching" -> result = "Watching"
            "reading" -> result = "Reading"
            "completed" -> result = "Completed"
            "on_hold" -> result = "On Hold"
            "dropped" -> result = "Dropped"
            "plan_to_watch" -> result = "Plan to Watch"
            "plan_to_read" -> result = "Plan to Read"
        }
        return result
    }
    fun formatListStatusInverted(status: String?): String {
        var result = status ?: ""
        when (status) {
            "Watching" -> result = "watching"
            "Reading" -> result = "reading"
            "Completed" -> result = "completed"
            "On Hold" -> result = "on_hold"
            "Dropped" -> result = "dropped"
            "Plan to Watch" -> result = "plan_to_watch"
            "Plan to Read" -> result = "plan_to_read"
        }
        return result
    }
    fun formatSeason(season: String): String {
        var result = season
        when (season) {
            "winter" -> result = "Winter"
            "spring" -> result = "Spring"
            "summer" -> result = "Summer"
            "fall" -> result = "Fall"
        }
        return result
    }
    fun formatSeasonInverted(season: String): String {
        var result = season
        when (season) {
            "Winter" -> result = "winter"
            "Spring" -> result = "spring"
            "Summer" -> result = "summer"
            "Fall" -> result = "fall"
        }
        return result
    }
    fun formatSource(source: String): String {
        var result = source
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
    fun formatWeekday(day: String?): String {
        var result = day ?: ""
        when (day) {
            "monday" -> result = "Monday"
            "tuesday" -> result = "Tuesday"
            "wednesday" -> result = "Wednesday"
            "thursday" -> result = "Thursday"
            "friday" -> result = "Friday"
            "saturday" -> result = "Saturday"
            "sunday" -> result = "Sunday"
        }
        return result
    }
}