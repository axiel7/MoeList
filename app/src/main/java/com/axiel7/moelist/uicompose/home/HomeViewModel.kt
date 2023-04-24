package com.axiel7.moelist.uicompose.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    nsfw: Int
): BaseViewModel() {

    fun initRequestChain() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            if (todayAnimes.isEmpty()) getTodayAiringAnimes()
            if (seasonAnimes.isEmpty()) getSeasonAnimes()
            if (recommendedAnimes.isEmpty()) getRecommendedAnimes()
            isLoading = false
        }
    }

    private val currentStartSeason = StartSeason(SeasonCalendar.currentYear, SeasonCalendar.currentSeasonStr)

    private val paramsToday = ApiParams(
        sort = Constants.SORT_ANIME_SCORE,
        nsfw = nsfw,
        fields = AnimeRepository.TODAY_FIELDS,
        limit = 500
    )
    var todayAnimes = mutableStateListOf<AnimeSeasonal>()
    suspend fun getTodayAiringAnimes() {
        val result = AnimeRepository.getSeasonalAnimes(
            apiParams = paramsToday,
            year = currentStartSeason.year,
            season = currentStartSeason.season
        )
        if (result?.data != null) {
            val tempList = mutableListOf<AnimeSeasonal>()
            for (anime in result.data) {
                if (anime.node.broadcast != null
                    && !todayAnimes.contains(anime)
                    && anime.node.broadcast.dayOfTheWeek == SeasonCalendar.currentJapanWeekday
                    && anime.node.startSeason == currentStartSeason
                    && anime.node.status == Constants.STATUS_AIRING
                ) { tempList.add(anime) }
            }
            tempList.sortByDescending { it.node.broadcast?.startTime }
            todayAnimes.clear()
            todayAnimes.addAll(tempList)
        } else {
            setErrorMessage(result?.message ?: "Generic error")
        }
    }

    private val paramsSeasonal = ApiParams(
        sort = Constants.SORT_ANIME_START_DATE,
        nsfw = nsfw
    )

    var seasonAnimes = mutableStateListOf<AnimeSeasonal>()
    suspend fun getSeasonAnimes() {
        val result = AnimeRepository.getSeasonalAnimes(
            apiParams = paramsSeasonal,
            year = currentStartSeason.year,
            season = currentStartSeason.season
        )
        if (result?.data != null) {
            seasonAnimes.clear()
            seasonAnimes.addAll(result.data)
        } else {
            setErrorMessage(result?.message ?: "Generic error")
        }
    }

    private val paramsRecommended = ApiParams(
        nsfw = nsfw
    )

    var recommendedAnimes = mutableStateListOf<AnimeList>()
    suspend fun getRecommendedAnimes() {
        val result = AnimeRepository.getRecommendedAnimes(paramsRecommended)
        if (result?.data != null) {
            recommendedAnimes.clear()
            recommendedAnimes.addAll(result.data)
        } else {
            setErrorMessage(result?.message ?: "Generic error")
        }
    }
}