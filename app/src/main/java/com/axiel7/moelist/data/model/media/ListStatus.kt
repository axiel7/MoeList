package com.axiel7.moelist.data.model.media

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ListStatus(
    val value: String,
    @DrawableRes val icon: Int
) : Localizable {
    @SerialName("watching")
    WATCHING(
        value = "watching",
        icon = R.drawable.play_circle_outline_24
    ),

    @SerialName("reading")
    READING(
        value = "reading",
        icon = R.drawable.play_circle_outline_24
    ),

    @SerialName("plan_to_watch")
    PTW(
        value = "plan_to_watch",
        icon = R.drawable.ic_round_access_time_24
    ),

    @SerialName("plan_to_read")
    PTR(
        value = "plan_to_read",
        icon = R.drawable.ic_round_access_time_24
    ),

    @SerialName("completed")
    COMPLETED(
        value = "completed",
        icon = R.drawable.check_circle_outline_24
    ),

    @SerialName("on_hold")
    ON_HOLD(
        value = "on_hold",
        icon = R.drawable.pause_circle_outline_24
    ),

    @SerialName("dropped")
    DROPPED(
        value = "dropped",
        icon = R.drawable.delete_outline_24
    );

    fun isCurrent() = this == WATCHING || this == READING

    fun isPlanning() = this == PTW || this == PTR

    @Composable
    override fun localized() = when (this) {
        WATCHING -> stringResource(R.string.watching)
        READING -> stringResource(R.string.reading)
        COMPLETED -> stringResource(R.string.completed)
        ON_HOLD -> stringResource(R.string.on_hold)
        DROPPED -> stringResource(R.string.dropped)
        PTW -> stringResource(R.string.ptw)
        PTR -> stringResource(R.string.ptr)
    }

    companion object {

        val listStatusAnimeValues = arrayOf(WATCHING, PTW, COMPLETED, ON_HOLD, DROPPED)

        val listStatusMangaValues = arrayOf(READING, PTR, COMPLETED, ON_HOLD, DROPPED)

        fun listStatusValues(mediaType: MediaType) =
            if (mediaType == MediaType.ANIME) listStatusAnimeValues else listStatusMangaValues
    }
}
