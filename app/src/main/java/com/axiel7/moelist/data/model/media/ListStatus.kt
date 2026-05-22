package com.axiel7.moelist.data.model.media

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.LocalizableAndColorable
import com.axiel7.moelist.ui.theme.stat_completed_content_dark
import com.axiel7.moelist.ui.theme.stat_completed_content_light
import com.axiel7.moelist.ui.theme.stat_completed_dark
import com.axiel7.moelist.ui.theme.stat_completed_light
import com.axiel7.moelist.ui.theme.stat_current_content_dark
import com.axiel7.moelist.ui.theme.stat_current_content_light
import com.axiel7.moelist.ui.theme.stat_current_dark
import com.axiel7.moelist.ui.theme.stat_current_light
import com.axiel7.moelist.ui.theme.stat_dropped_content_dark
import com.axiel7.moelist.ui.theme.stat_dropped_content_light
import com.axiel7.moelist.ui.theme.stat_dropped_dark
import com.axiel7.moelist.ui.theme.stat_dropped_light
import com.axiel7.moelist.ui.theme.stat_on_hold_content_dark
import com.axiel7.moelist.ui.theme.stat_on_hold_content_light
import com.axiel7.moelist.ui.theme.stat_on_hold_dark
import com.axiel7.moelist.ui.theme.stat_on_hold_light
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ListStatus(
    val icon: ImageVector
) : LocalizableAndColorable {
    @SerialName("watching")
    WATCHING(
        icon = Icons.Rounded.PlayCircleOutline
    ),

    @SerialName("reading")
    READING(
        icon = Icons.Rounded.PlayCircleOutline
    ),

    @SerialName("plan_to_watch")
    PLAN_TO_WATCH(
        icon = Icons.Rounded.Schedule
    ),

    @SerialName("plan_to_read")
    PLAN_TO_READ(
        icon = Icons.Rounded.Schedule
    ),

    @SerialName("completed")
    COMPLETED(
        icon = Icons.Rounded.CheckCircleOutline
    ),

    @SerialName("on_hold")
    ON_HOLD(
        icon = Icons.Rounded.PauseCircleOutline
    ),

    @SerialName("dropped")
    DROPPED(
        icon = Icons.Rounded.HighlightOff
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
        PLAN_TO_WATCH, PLAN_TO_READ -> MaterialTheme.colorScheme.outline
        COMPLETED -> if (isSystemInDarkTheme()) stat_completed_dark else stat_completed_light
        ON_HOLD -> if (isSystemInDarkTheme()) stat_on_hold_dark else stat_on_hold_light
        DROPPED -> if (isSystemInDarkTheme()) stat_dropped_dark else stat_dropped_light
    }

    @Composable
    override fun onPrimaryColor() = when (this) {
        WATCHING, READING -> if (isSystemInDarkTheme()) stat_current_content_dark else stat_current_content_light
        PLAN_TO_WATCH, PLAN_TO_READ -> if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.inverseOnSurface
        COMPLETED -> if (isSystemInDarkTheme()) stat_completed_content_dark else stat_completed_content_light
        ON_HOLD -> if (isSystemInDarkTheme()) stat_on_hold_content_dark else stat_on_hold_content_light
        DROPPED -> if (isSystemInDarkTheme()) stat_dropped_content_dark else stat_dropped_content_light
    }

    companion object {

        val listStatusAnimeValues = arrayOf(WATCHING, PLAN_TO_WATCH, COMPLETED, ON_HOLD, DROPPED)

        val listStatusMangaValues = arrayOf(READING, PLAN_TO_READ, COMPLETED, ON_HOLD, DROPPED)

        fun listStatusValues(mediaType: MediaType) =
            if (mediaType == MediaType.ANIME) listStatusAnimeValues else listStatusMangaValues

        val navType = object : NavType<ListStatus?>(isNullableAllowed = true) {
            override fun get(bundle: Bundle, key: String): ListStatus? {
                return try {
                    bundle.getString(key)?.let {
                        ListStatus.valueOf(it)
                    }
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            override fun parseValue(value: String): ListStatus? {
                if (value == "null") return null
                return try {
                    ListStatus.valueOf(value)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            override fun put(bundle: Bundle, key: String, value: ListStatus?) {
                bundle.putString(key, value?.name)
            }
        }
    }
}
