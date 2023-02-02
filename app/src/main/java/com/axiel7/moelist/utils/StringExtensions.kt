package com.axiel7.moelist.utils

import android.content.Context
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus

/** Helper functions to localize API values */
object StringExtensions {
    fun String.formatMediaType(context: Context) =
        when (this) {
            "tv" -> context.getString(R.string.tv)
            "ova" -> context.getString(R.string.ova)
            "ona" -> context.getString(R.string.ona)
            "movie" -> context.getString(R.string.movie)
            "special" -> context.getString(R.string.special)
            "music" -> context.getString(R.string.music)
            "unknown" -> context.getString(R.string.unknown)
            "manga" -> context.getString(R.string.manga)
            "one_shot" -> context.getString(R.string.one_shot)
            "manhwa" -> context.getString(R.string.manhwa)
            "manhua" -> context.getString(R.string.manhua)
            "novel" -> context.getString(R.string.novel)
            "light_novel" -> context.getString(R.string.light_novel)
            "doujinshi" -> context.getString(R.string.doujinshi)
            else -> this
        }

    fun String.formatStatus(context: Context) =
        when (this) {
            "currently_airing" -> context.getString(R.string.airing)
            "finished_airing" -> context.getString(R.string.finished)
            "not_yet_aired" -> context.getString(R.string.not_yet_aired)
            "currently_publishing" -> context.getString(R.string.publishing)
            "finished" -> context.getString(R.string.finished)
            "on_hiatus" -> context.getString(R.string.on_hiatus)
            "discontinued" -> context.getString(R.string.discontinued)
            else -> this
        }

    fun ListStatus?.formatListStatus(context: Context) =
        when (this) {
            ListStatus.WATCHING -> context.getString(R.string.watching)
            ListStatus.READING -> context.getString(R.string.reading)
            ListStatus.COMPLETED -> context.getString(R.string.completed)
            ListStatus.ON_HOLD -> context.getString(R.string.on_hold)
            ListStatus.DROPPED -> context.getString(R.string.dropped)
            ListStatus.PTW -> context.getString(R.string.ptw)
            ListStatus.PTR -> context.getString(R.string.ptr)
            else -> ""
        }

    fun String?.formatListStatusInverted(context: Context) =
        when (this) {
            context.getString(R.string.watching) -> ListStatus.WATCHING
            context.getString(R.string.reading) -> ListStatus.READING
            context.getString(R.string.completed) -> ListStatus.COMPLETED
            context.getString(R.string.on_hold) -> ListStatus.ON_HOLD
            context.getString(R.string.dropped) -> ListStatus.DROPPED
            context.getString(R.string.ptw) -> ListStatus.PTW
            context.getString(R.string.ptr) -> ListStatus.PTR
            else -> ListStatus.COMPLETED
        }

    fun String.formatSeason(context: Context) =
        when (this) {
            "winter" -> context.getString(R.string.winter)
            "spring" -> context.getString(R.string.spring)
            "summer" -> context.getString(R.string.summer)
            "fall" -> context.getString(R.string.fall)
            else -> this
        }

    fun String.formatSeasonInverted(context: Context) =
        when (this) {
            context.getString(R.string.winter) -> "winter"
            context.getString(R.string.spring) -> "spring"
            context.getString(R.string.summer) -> "summer"
            context.getString(R.string.fall) -> "fall"
            else -> "winter"
        }

    fun String.formatSource(context: Context) =
        when (this) {
            "original" -> context.getString(R.string.original)
            "manga" -> context.getString(R.string.manga)
            "light_novel" -> context.getString(R.string.light_novel)
            "visual_novel" -> context.getString(R.string.visual_novel)
            "game" -> context.getString(R.string.game)
            "web_manga" -> context.getString(R.string.web_manga)
            "music" -> context.getString(R.string.music)
            "4_koma_manga" -> "4-Koma manga"
            else -> this
        }

    fun Int.formatScore(context: Context) =
        when (this) {
            0 -> "─"
            1 -> context.getString(R.string.score_apalling)
            2 -> context.getString(R.string.score_horrible)
            3 -> context.getString(R.string.score_very_bad)
            4 -> context.getString(R.string.score_bad)
            5 -> context.getString(R.string.score_average)
            6 -> context.getString(R.string.score_fine)
            7 -> context.getString(R.string.score_good)
            8 -> context.getString(R.string.score_very_good)
            9 -> context.getString(R.string.score_great)
            10 -> context.getString(R.string.score_masterpiece)
            else -> "─"
        }

    fun String?.formatWeekday(context: Context) =
        when (this) {
            "monday" -> context.getString(R.string.monday)
            "tuesday" -> context.getString(R.string.tuesday)
            "wednesday" -> context.getString(R.string.wednesday)
            "thursday" -> context.getString(R.string.thursday)
            "friday" -> context.getString(R.string.friday)
            "saturday" -> context.getString(R.string.saturday)
            "sunday" -> context.getString(R.string.sunday)
            else -> this ?: ""
        }

    fun String.formatSortOption(context: Context) =
        when (this) {
            "anime_title" -> context.getString(R.string.sort_title)
            "manga_title" -> context.getString(R.string.sort_title)
            "list_score" -> context.getString(R.string.sort_score)
            "list_updated_at" -> context.getString(R.string.sort_last_updated)
            else -> this
        }

    fun String?.formatGenre(context: Context) =
        when (this) {
            "Action" -> context.getString(R.string.genre_action)
            "Adventure" -> context.getString(R.string.genre_adventure)
            "Cars" -> context.getString(R.string.genre_cars)
            "Comedy" -> context.getString(R.string.genre_comedy)
            "Dementia" -> context.getString(R.string.genre_dementia)
            "Demons" -> context.getString(R.string.genre_demons)
            "Drama" -> context.getString(R.string.genre_drama)
            "Ecchi" -> context.getString(R.string.genre_ecchi)
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
            else -> this ?: ""
        }

    fun String.formatRelation(context: Context) =
        when (this) {
            "Prequel" -> context.getString(R.string.relation_prequel)
            "Sequel" -> context.getString(R.string.relation_sequel)
            "Summary" -> context.getString(R.string.relation_summary)
            "Alternative version" -> context.getString(R.string.relation_alternative_version)
            "Alternative setting" -> context.getString(R.string.relation_alternative_setting)
            "Spin-off" -> context.getString(R.string.relation_spin_off)
            "Side story" -> context.getString(R.string.relation_side_story)
            "Parent story" -> context.getString(R.string.parent_story)
            "Full story" -> context.getString(R.string.full_story)
            "Adaptation" -> context.getString(R.string.adaptation)
            "Other" -> context.getString(R.string.relation_other)
            else -> this
        }

}