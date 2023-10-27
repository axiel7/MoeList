package com.axiel7.moelist.uicompose.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val animeRepository: AnimeRepository
) : BaseViewModel() {

    fun initRequestChain(isLoggedIn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            if (todayAnimes.isEmpty()) getTodayAiringAnimes()
            if (seasonAnimes.isEmpty()) getSeasonAnimes()
            if (isLoggedIn && recommendedAnimes.isEmpty()) getRecommendedAnimes()
            isLoading = false
        }
    }

    val todayAnimes = mutableStateListOf<AnimeRanking>()
    private suspend fun getTodayAiringAnimes() {
        val result = animeRepository.getAnimeRanking(
            rankingType = RankingType.AIRING,
            limit = 100,
            fields = AnimeRepository.TODAY_FIELDS
        )
        if (result?.data != null) {
            val tempList = mutableListOf<AnimeRanking>()
            for (anime in result.data) {
                if (anime.node.broadcast != null
                    && !todayAnimes.contains(anime)
                    && anime.node.broadcast.dayOfTheWeek == SeasonCalendar.currentJapanWeekday
                    && anime.node.status == MediaStatus.AIRING
                ) {
                    tempList.add(anime)
                }
            }
            tempList.sortByDescending { it.node.broadcast?.startTime }
            todayAnimes.clear()
            todayAnimes.addAll(tempList)
        } else {
            setErrorMessage(result?.message ?: result?.error ?: GENERIC_ERROR)
        }
    }

    val seasonAnimes = mutableStateListOf<AnimeSeasonal>()
    private suspend fun getSeasonAnimes() {
        val currentStartSeason = SeasonCalendar.currentStartSeason
        val result = animeRepository.getSeasonalAnimes(
            sort = MediaSort.ANIME_START_DATE,
            year = currentStartSeason.year,
            season = currentStartSeason.season,
            limit = 25,
            fields = "alternative_titles{en,ja},mean",
        )
        if (result?.data != null) {
            seasonAnimes.clear()
            seasonAnimes.addAll(result.data)
        } else {
            setErrorMessage(result?.message ?: result?.error ?: GENERIC_ERROR)
        }
    }

    val recommendedAnimes = mutableStateListOf<AnimeList>()
    private suspend fun getRecommendedAnimes() {
        val result = animeRepository.getRecommendedAnimes(
            limit = 25
        )
        if (result?.data != null) {
            recommendedAnimes.clear()
            recommendedAnimes.addAll(result.data)
        } else {
            setErrorMessage(result?.message ?: result?.error ?: GENERIC_ERROR)
        }
    }
}