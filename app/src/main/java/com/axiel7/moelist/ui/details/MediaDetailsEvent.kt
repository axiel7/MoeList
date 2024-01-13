package com.axiel7.moelist.ui.details

import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.ui.base.event.UiEvent
import java.time.LocalDate
import java.time.LocalTime

interface MediaDetailsEvent : UiEvent {

    fun onChangedMyListStatus(value: BaseMyListStatus?, removed: Boolean = false)

    fun getCharacters()

    fun scheduleAiringAnimeNotification(
        title: String,
        animeId: Int,
        weekDay: WeekDay,
        jpHour: LocalTime
    )

    fun scheduleAnimeStartNotification(
        title: String,
        animeId: Int,
        startDate: LocalDate,
    )

    fun removeAiringAnimeNotification(animeId: Int)
}