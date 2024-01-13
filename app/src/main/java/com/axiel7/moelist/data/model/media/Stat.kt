package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.data.model.base.LocalizableAndColorable

data class Stat<T : LocalizableAndColorable>(
    val type: T,
    val value: Float,
) {
    companion object {
        val exampleStats = listOf(
            Stat(
                type = ListStatus.WATCHING,
                value = 12f,
            ),
            Stat(
                type = ListStatus.COMPLETED,
                value = 120f,
            ),
            Stat(
                type = ListStatus.ON_HOLD,
                value = 5f,
            ),
            Stat(
                type = ListStatus.DROPPED,
                value = 3f,
            ),
            Stat(
                type = ListStatus.PLAN_TO_WATCH,
                value = 30f,
            ),
        )
    }
}
