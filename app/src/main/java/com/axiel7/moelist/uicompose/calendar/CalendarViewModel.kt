package com.axiel7.moelist.uicompose.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val animeRepository: AnimeRepository
) : BaseViewModel() {

    var weekAnime by mutableStateOf(
        arrayOf<MutableList<AnimeRanking>>(
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
            val result = animeRepository.getAnimeRanking(
                rankingType = RankingType.AIRING,
                limit = 300,
                fields = AnimeRepository.CALENDAR_FIELDS
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