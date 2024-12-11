package com.axiel7.moelist.ui.home

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val animeRepository: AnimeRepository
) : BaseViewModel<HomeUiState>(), HomeEvent {

    override val mutableUiState = MutableStateFlow(HomeUiState())

    override fun initRequestChain(isLoggedIn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.value.run {
                setLoading(true)
                if (todayAnimes.isEmpty()) getTodayAiringAnimes()
                if (seasonAnimes.isEmpty()) getSeasonAnimes()
                if (isLoggedIn && recommendedAnimes.isEmpty()) getRecommendedAnimes()
                setLoading(false)
            }
        }
    }

    private suspend fun getTodayAiringAnimes() {
        val result = animeRepository.getAnimeRanking(
            rankingType = RankingType.AIRING,
            limit = 100,
            fields = AnimeRepository.TODAY_FIELDS
        )
        if (result.data != null) {
            val tempList = mutableListOf<AnimeRanking>()
            for (anime in result.data) {
                if (anime.node.broadcast != null
                    && !tempList.contains(anime)
                    && anime.node.broadcast.dayOfTheWeek == SeasonCalendar.currentJapanWeekday
                    && anime.node.status == MediaStatus.AIRING
                ) {
                    tempList.add(anime)
                }
            }
            tempList.sortByDescending { it.node.broadcast?.startTime }
            mutableUiState.update { it.copy(todayAnimes = tempList) }
        } else {
            showMessage(result.message ?: result.error)
        }
    }

    private suspend fun getSeasonAnimes() {
        val currentStartSeason = SeasonCalendar.currentStartSeason()
        val result = animeRepository.getSeasonalAnimes(
            sort = MediaSort.ANIME_START_DATE,
            year = currentStartSeason.year,
            season = currentStartSeason.season,
            limit = 25,
            fields = "alternative_titles{en,ja},mean",
        )
        if (result.data != null) {
            mutableUiState.update { it.copy(seasonAnimes = result.data) }
        } else {
            showMessage(result.message ?: result.error)
        }
    }

    private suspend fun getRecommendedAnimes() {
        val result = animeRepository.getRecommendedAnimes(
            limit = 25
        )
        if (result.data != null) {
            mutableUiState.update { it.copy(recommendedAnimes = result.data) }
        } else {
            showMessage(result.message ?: result.error)
        }
    }
}