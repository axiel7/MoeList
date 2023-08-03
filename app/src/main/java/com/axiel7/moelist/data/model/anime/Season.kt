package com.axiel7.moelist.data.model.anime

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Season(
    val value: String,
    @DrawableRes val icon: Int
) : Localizable {
    @SerialName("winter")
    WINTER(
        value = "winter",
        icon = R.drawable.ic_winter_24
    ),

    @SerialName("spring")
    SPRING(
        value = "spring",
        icon = R.drawable.ic_spring_24
    ),

    @SerialName("summer")
    SUMMER(
        value = "summer",
        icon = R.drawable.ic_summer_24
    ),

    @SerialName("fall")
    FALL(
        value = "fall",
        icon = R.drawable.ic_fall_24
    );

    @Composable
    override fun localized() = when (this) {
        WINTER -> stringResource(R.string.winter)
        SPRING -> stringResource(R.string.spring)
        SUMMER -> stringResource(R.string.summer)
        FALL -> stringResource(R.string.fall)
    }
}