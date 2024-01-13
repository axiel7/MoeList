package com.axiel7.moelist.data.model.media

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.LocalizableAndColorable
import com.axiel7.moelist.ui.theme.stat_completed_dark
import com.axiel7.moelist.ui.theme.stat_completed_light
import com.axiel7.moelist.ui.theme.stat_current_dark
import com.axiel7.moelist.ui.theme.stat_current_light
import com.axiel7.moelist.ui.theme.stat_dropped_dark
import com.axiel7.moelist.ui.theme.stat_dropped_light
import com.axiel7.moelist.ui.theme.stat_on_hold_dark
import com.axiel7.moelist.ui.theme.stat_on_hold_light
import com.axiel7.moelist.ui.theme.stat_planned_dark
import com.axiel7.moelist.ui.theme.stat_planned_light
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ListStatus(
    @DrawableRes val icon: Int
) : LocalizableAndColorable {
    @SerialName("watching")
    WATCHING(
        icon = R.drawable.play_circle_outline_24
    ),

    @SerialName("reading")
    READING(
        icon = R.drawable.play_circle_outline_24
    ),

    @SerialName("plan_to_watch")
    PLAN_TO_WATCH(
        icon = R.drawable.ic_round_access_time_24
    ),

    @SerialName("plan_to_read")
    PLAN_TO_READ(
        icon = R.drawable.ic_round_access_time_24
    ),

    @SerialName("completed")
    COMPLETED(
        icon = R.drawable.check_circle_outline_24
    ),

    @SerialName("on_hold")
    ON_HOLD(
        icon = R.drawable.pause_circle_outline_24
    ),

    @SerialName("dropped")
    DROPPED(
        icon = R.drawable.delete_outline_24
    );

    val value get() = name.lowercase()

    fun isCurrent() = this == WATCHING || this == READING

    fun isPlanning() = this == PLAN_TO_WATCH || this == PLAN_TO_READ

    @Composable
    override fun localized() = stringResource(stringRes)

    val stringRes
        get() = when (this) {
            WATCHING -> R.string.watching
            READING -> R.string.reading
            COMPLETED -> R.string.completed
            ON_HOLD -> R.string.on_hold
            DROPPED -> R.string.dropped
            PLAN_TO_WATCH -> R.string.ptw
            PLAN_TO_READ -> R.string.ptr
        }

    @Composable
    override fun primaryColor() = when (this) {
        WATCHING, READING -> if (isSystemInDarkTheme()) stat_current_dark else stat_current_light
        PLAN_TO_WATCH, PLAN_TO_READ -> if (isSystemInDarkTheme()) stat_planned_dark else stat_planned_light
        COMPLETED -> if (isSystemInDarkTheme()) stat_completed_dark else stat_completed_light
        ON_HOLD -> if (isSystemInDarkTheme()) stat_on_hold_dark else stat_on_hold_light
        DROPPED -> if (isSystemInDarkTheme()) stat_dropped_dark else stat_dropped_light
    }

    @Composable
    override fun onPrimaryColor() = MaterialTheme.colorScheme.contentColorFor(primaryColor())

    companion object {

        val listStatusAnimeValues = arrayOf(WATCHING, PLAN_TO_WATCH, COMPLETED, ON_HOLD, DROPPED)

        val listStatusMangaValues = arrayOf(READING, PLAN_TO_READ, COMPLETED, ON_HOLD, DROPPED)

        fun listStatusValues(mediaType: MediaType) =
            if (mediaType == MediaType.ANIME) listStatusAnimeValues else listStatusMangaValues
    }
}
