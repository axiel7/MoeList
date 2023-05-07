package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Season(val value: String) {
    @SerialName("winter")
    WINTER("winter"),
    @SerialName("spring")
    SPRING("spring"),
    @SerialName("summer")
    SUMMER("summer"),
    @SerialName("fall")
    FALL("fall")
}

@Composable
fun Season.localized() = when (this) {
    Season.WINTER -> stringResource(R.string.winter)
    Season.SPRING -> stringResource(R.string.spring)
    Season.SUMMER -> stringResource(R.string.summer)
    Season.FALL -> stringResource(R.string.fall)
}

fun Season.icon() = when (this) {
    Season.WINTER -> R.drawable.ic_winter_24
    Season.SPRING -> R.drawable.ic_spring_24
    Season.SUMMER -> R.drawable.ic_summer_24
    Season.FALL -> R.drawable.ic_fall_24
}