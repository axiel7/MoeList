package com.axiel7.moelist.uicompose.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarViewModel : BaseViewModel() {

    private val params = ApiParams(
        sort = MediaSort.ANIME_NUM_USERS.value,
        nsfw = App.nsfw,
        fields = AnimeRepository.CALENDAR_FIELDS,
        limit = 300
    )

    var weekAnime by mutableStateOf(
        arrayOf<MutableList<AnimeSeasonal>>(
            mutableListOf(),//0: MONDAY
            mutableListOf(),//1: TUESDAY
            mutableListOf(),//2: WEDNESDAY
            mutableListOf(),//3: THURSDAY
            mutableListOf(),//4: FRIDAY
            mutableListOf(),//5: SATURDAY
            mutableListOf(),//6: SUNDAY
        )
    )

    fun getSeasonAnime() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val result = AnimeRepository.getSeasonalAnimes(
                apiParams = params,
                year = SeasonCalendar.currentYear,
                season = SeasonCalendar.currentSeason
            )

            if (result?.data == null || result.message != null) {
                setErrorMessage(result?.message ?: "Generic error")
            } else {
                result.data.forEach { anime ->
                    anime.node.broadcast?.dayOfTheWeek?.let { day ->
                        weekAnime[day.numeric - 1].add(anime)
                    }
                }
                weekAnime = weekAnime.copyOf()
            }
            isLoading = false
        }
    }
}