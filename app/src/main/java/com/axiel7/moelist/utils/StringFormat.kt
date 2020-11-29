package com.axiel7.moelist.utils

import android.content.Context
import com.axiel7.moelist.R

object StringFormat {
    fun formatMediaType(mediaType: String, context: Context): String {
        var result = mediaType
        when (mediaType) {
            "tv" -> result = context.getString(R.string.tv)
            "ova" -> result = context.getString(R.string.ova)
            "ona" -> result = context.getString(R.string.ona)
            "movie" -> result = context.getString(R.string.movie)
            "special" -> result = context.getString(R.string.special)
            "music" -> result = context.getString(R.string.music)
            "unknown" -> result = context.getString(R.string.unknown)
            "manga" -> result = context.getString(R.string.manga)
            "one_shot" -> result = context.getString(R.string.one_shot)
            "manhwa" -> result = context.getString(R.string.manhwa)
            "novel" -> result = context.getString(R.string.novel)
            "doujinshi" -> result = context.getString(R.string.doujinshi)
        }
        return result
    }
    fun formatStatus(status: String, context: Context): String {
        var result = status
        when (status) {
            "currently_airing" -> result = context.getString(R.string.airing)
            "finished_airing" -> result = context.getString(R.string.finished)
            "not_yet_aired" -> result = context.getString(R.string.not_yet_aired)
            "currently_publishing" -> result = context.getString(R.string.publishing)
            "finished" -> result = context.getString(R.string.finished)
            "on_hiatus" -> result = context.getString(R.string.on_hiatus)
        }
        return result
    }
    fun formatListStatus(status: String?, context: Context): String {
        var result = status ?: ""
        when (status) {
            "watching" -> result = context.getString(R.string.watching)
            "reading" -> result = context.getString(R.string.reading)
            "completed" -> result = context.getString(R.string.completed)
            "on_hold" -> result = context.getString(R.string.on_hold)
            "dropped" -> result = context.getString(R.string.dropped)
            "plan_to_watch" -> result = context.getString(R.string.ptw)
            "plan_to_read" -> result = context.getString(R.string.ptr)
        }
        return result
    }
    fun formatListStatusInverted(status: String?, context: Context): String {
        var result = status ?: ""
        when (status) {
            context.getString(R.string.watching) -> result = "watching"
            context.getString(R.string.reading) -> result = "reading"
            context.getString(R.string.completed) -> result = "completed"
            context.getString(R.string.on_hold) -> result = "on_hold"
            context.getString(R.string.dropped) -> result = "dropped"
            context.getString(R.string.ptw) -> result = "plan_to_watch"
            context.getString(R.string.ptr) -> result = "plan_to_read"
        }
        return result
    }
    fun formatSeason(season: String, context: Context): String {
        var result = season
        when (season) {
            "winter" -> result = context.getString(R.string.winter)
            "spring" -> result = context.getString(R.string.spring)
            "summer" -> result = context.getString(R.string.summer)
            "fall" -> result = context.getString(R.string.fall)
        }
        return result
    }
    fun formatSeasonInverted(season: String, context: Context): String {
        var result = season
        when (season) {
            context.getString(R.string.winter) -> result = "winter"
            context.getString(R.string.spring) -> result = "spring"
            context.getString(R.string.summer) -> result = "summer"
            context.getString(R.string.fall) -> result = "fall"
        }
        return result
    }
    fun formatSource(source: String, context: Context): String {
        var result = source
        when (source) {
            "original" -> result = context.getString(R.string.original)
            "manga" -> result = context.getString(R.string.manga)
            "light_novel" -> result = context.getString(R.string.light_novel)
            "visual_novel" -> result = context.getString(R.string.visual_novel)
            "game" -> result = context.getString(R.string.game)
            "web_manga" -> result = context.getString(R.string.web_manga)
            "music" -> result = context.getString(R.string.music)
        }
        return result
    }
    fun formatScore(score: Int, context: Context): String {
        var result = ""
        when (score) {
            0 -> result = "─"
            1 -> result = context.getString(R.string.score_apalling)
            2 -> result = context.getString(R.string.score_horrible)
            3 -> result = context.getString(R.string.score_very_bad)
            4 -> result = context.getString(R.string.score_bad)
            5 -> result = context.getString(R.string.score_average)
            6 -> result = context.getString(R.string.score_fine)
            7 -> result = context.getString(R.string.score_good)
            8 -> result = context.getString(R.string.score_very_good)
            9 -> result = context.getString(R.string.score_great)
            10 -> result = context.getString(R.string.score_masterpiece)
        }
        return result
    }
    fun formatWeekday(day: String?, context: Context): String {
        var result = day ?: ""
        when (day) {
            "monday" -> result = context.getString(R.string.monday)
            "tuesday" -> result = context.getString(R.string.tuesday)
            "wednesday" -> result = context.getString(R.string.wednesday)
            "thursday" -> result = context.getString(R.string.thursday)
            "friday" -> result = context.getString(R.string.friday)
            "saturday" -> result = context.getString(R.string.saturday)
            "sunday" -> result = context.getString(R.string.sunday)
        }
        return result
    }
    fun formatSortOption(sort: String, context: Context): String {
        var result = sort
        when (sort) {
            "anime_title" -> result = context.getString(R.string.sort_title)
            "manga_title" -> result = context.getString(R.string.sort_title)
            "list_score" -> result = context.getString(R.string.sort_score)
            "list_updated_at" -> result = context.getString(R.string.sort_last_updated)
        }
        return result
    }

    fun formatGenre(genre: String?, context: Context): String {
        return when (genre) {
            "Action" -> context.getString(R.string.genre_action)
            "Adventure" -> context.getString(R.string.genre_adventure)
            "Cars" -> context.getString(R.string.genre_cars)
            "Comedy" -> context.getString(R.string.genre_comedy)
            "Dementia" -> context.getString(R.string.genre_dementia)
            "Demons" -> context.getString(R.string.genre_demons)
            "Drama" -> context.getString(R.string.genre_drama)
            "Ecchi" -> context.getString(R.string.genre_drama)
            "Fantasy" -> context.getString(R.string.genre_fantasy)
            "Game" -> context.getString(R.string.genre_game)
            "Harem" -> context.getString(R.string.genre_harem)
            "Hentai" -> context.getString(R.string.genre_hentai)
            "Historical" -> context.getString(R.string.genre_historical)
            "Horror" -> context.getString(R.string.genre_horror)
            "Josei" -> context.getString(R.string.genre_josei)
            "Kids" -> context.getString(R.string.genre_kids)
            "Magic" -> context.getString(R.string.genre_magic)
            "Martial Arts" -> context.getString(R.string.genre_martial_arts)
            "Mecha" -> context.getString(R.string.genre_mecha)
            "Military" -> context.getString(R.string.genre_military)
            "Music" -> context.getString(R.string.genre_music)
            "Mystery" -> context.getString(R.string.genre_mystery)
            "Parody" -> context.getString(R.string.genre_parody)
            "Police" -> context.getString(R.string.genre_police)
            "Psychological" -> context.getString(R.string.genre_psychological)
            "Romance" -> context.getString(R.string.genre_romance)
            "Samurai" -> context.getString(R.string.genre_samurai)
            "School" -> context.getString(R.string.genre_school)
            "Sci-Fi" -> context.getString(R.string.genre_sci_fi)
            "Seinen" -> context.getString(R.string.genre_seinen)
            "Shoujo" -> context.getString(R.string.genre_shoujo)
            "Shoujo Ai" -> context.getString(R.string.genre_shoujo_ai)
            "Shounen" -> context.getString(R.string.genre_shounen)
            "Shounen Ai" -> context.getString(R.string.genre_shounen_ai)
            "Slice of Life" -> context.getString(R.string.genre_slice_of_life)
            "Space" -> context.getString(R.string.genre_space)
            "Sports" -> context.getString(R.string.genre_sports)
            "Super Power" -> context.getString(R.string.genre_superpower)
            "Supernatural" -> context.getString(R.string.genre_supernatural)
            "Thriller" -> context.getString(R.string.genre_thriller)
            "Vampire" -> context.getString(R.string.genre_vampire)
            "Yaoi" -> context.getString(R.string.genre_yaoi)
            "Yuri" -> context.getString(R.string.genre_yuri)
            else -> genre ?: ""
        }
    }

    fun formatRelation(relation: String, context: Context): String {
        return when (relation) {
            "Prequel" -> context.getString(R.string.relation_prequel)
            "Sequel" -> context.getString(R.string.relation_sequel)
            "Summary" -> context.getString(R.string.relation_summary)
            "Alternative version" -> context.getString(R.string.relation_alternative_version)
            "Alternative setting" -> context.getString(R.string.relation_alternative_setting)
            "Spin-off" -> context.getString(R.string.relation_spin_off)
            "Side story" -> context.getString(R.string.relation_side_story)
            else -> relation
        }
    }
}