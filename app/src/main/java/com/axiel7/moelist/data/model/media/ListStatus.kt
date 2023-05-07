package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ListStatus(val value: String) {
    @SerialName("watching")
    WATCHING("watching"),
    @SerialName("reading")
    READING("reading"),
    @SerialName("plan_to_watch")
    PTW("plan_to_watch"),
    @SerialName("plan_to_read")
    PTR("plan_to_read"),
    @SerialName("completed")
    COMPLETED("completed"),
    @SerialName("on_hold")
    ON_HOLD("on_hold"),
    @SerialName("dropped")
    DROPPED("dropped")
}

val listStatusAnimeValues get() =
    arrayOf(ListStatus.WATCHING, ListStatus.PTW, ListStatus.COMPLETED, ListStatus.ON_HOLD, ListStatus.DROPPED)

val listStatusMangaValues get() =
    arrayOf(ListStatus.READING, ListStatus.PTR, ListStatus.COMPLETED, ListStatus.ON_HOLD, ListStatus.DROPPED)

fun ListStatus.isCurrent() = this == ListStatus.WATCHING || this == ListStatus.READING

fun ListStatus.isPlanning() = this == ListStatus.PTW || this == ListStatus.PTR

@Composable
fun ListStatus.localized() = when (this) {
    ListStatus.WATCHING -> stringResource(R.string.watching)
    ListStatus.READING -> stringResource(R.string.reading)
    ListStatus.COMPLETED -> stringResource(R.string.completed)
    ListStatus.ON_HOLD -> stringResource(R.string.on_hold)
    ListStatus.DROPPED -> stringResource(R.string.dropped)
    ListStatus.PTW -> stringResource(R.string.ptw)
    ListStatus.PTR -> stringResource(R.string.ptr)
}

@Composable
fun String.listStatusDelocalized() = when (this) {
    stringResource(R.string.watching) -> ListStatus.WATCHING
    stringResource(R.string.reading) -> ListStatus.READING
    stringResource(R.string.completed) -> ListStatus.COMPLETED
    stringResource(R.string.on_hold) -> ListStatus.ON_HOLD
    stringResource(R.string.dropped) -> ListStatus.DROPPED
    stringResource(R.string.ptw) -> ListStatus.PTW
    stringResource(R.string.ptr) -> ListStatus.PTR
    else -> ListStatus.COMPLETED
}

fun ListStatus.icon() = when (this) {
    ListStatus.WATCHING, ListStatus.READING -> R.drawable.play_circle_outline_24
    ListStatus.COMPLETED -> R.drawable.check_circle_outline_24
    ListStatus.ON_HOLD -> R.drawable.pause_circle_outline_24
    ListStatus.DROPPED -> R.drawable.delete_outline_24
    ListStatus.PTW, ListStatus.PTR -> R.drawable.ic_round_access_time_24
}

