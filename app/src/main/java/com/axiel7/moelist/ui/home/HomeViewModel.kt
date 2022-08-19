package com.axiel7.moelist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.paging.AnimeRecommendPaging
import com.axiel7.moelist.data.paging.AnimeSeasonalPaging
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.RESPONSE_NONE
import com.axiel7.moelist.utils.Constants.RESPONSE_OK
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val nsfw = MutableStateFlow(0)
    fun setNsfw(value: Int) {
        nsfw.value = value
        paramsSeasonal.value.nsfw = value
        paramsRecommend.value.nsfw = value
    }

    private val paramsSeasonal = MutableStateFlow(
        ApiParams(
            sort = Constants.SORT_ANIME_START_DATE,
            nsfw = nsfw.value
        )
    )

    val animeSeasonalFlow =
        Pager(
            PagingConfig(pageSize = 15, prefetchDistance = 5)
        ) {
            AnimeSeasonalPaging(
                api = App.api,
                apiParams = paramsSeasonal.value,
                startSeason = StartSeason(
                    SeasonCalendar.currentYear,
                    SeasonCalendar.currentSeasonStr
                )
            )
        }.flow
            .cachedIn(viewModelScope)


    private val paramsRecommend = MutableStateFlow(
        ApiParams(
            nsfw = nsfw.value
        )
    )

    val animeRecommendFlow =
        Pager(
            PagingConfig(pageSize = 15, prefetchDistance = 5)
        ) {
            AnimeRecommendPaging(App.api, paramsRecommend.value)
        }.flow
            .cachedIn(viewModelScope)


    private val _todayResponse = MutableStateFlow(listOf<AnimeSeasonal>() to RESPONSE_NONE)
    val todayResponse: StateFlow<Pair<List<AnimeSeasonal>, String>> = _todayResponse
    private val paramsToday = MutableStateFlow(
        ApiParams(
            sort = Constants.SORT_ANIME_SCORE,
            nsfw = nsfw.value,
            fields = FIELDS_TODAY,
            limit = 500
        )
    )

    private fun getTodayAnimes(page: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {

            val result = try {
                if (page == null) App.api.getSeasonalAnime(
                    params = paramsToday.value,
                    year = SeasonCalendar.currentYear,
                    season = SeasonCalendar.currentSeasonStr
                )
                else App.api.getSeasonalAnime(page)
            } catch (e: Exception) {
                null
            }

            if (result?.data != null) {
                val jpDayOfWeek = SeasonCalendar.currentJapanWeekday
                val currentStartSeason =
                    StartSeason(SeasonCalendar.currentYear, SeasonCalendar.currentSeasonStr)
                val tempList = mutableListOf<AnimeSeasonal>()
                for (anime in result.data) {
                    if (anime.node.broadcast != null
                        && !_todayResponse.value.first.contains(anime)
                        && anime.node.broadcast.dayOfTheWeek == jpDayOfWeek
                        && anime.node.startSeason == currentStartSeason
                        && anime.node.status == Constants.STATUS_AIRING
                    ) { tempList.add(anime) }
                }
                tempList.sortByDescending { it.node.broadcast?.startTime }
                _todayResponse.value = tempList to RESPONSE_OK
            }
            else {
                _todayResponse.value = emptyList<AnimeSeasonal>() to RESPONSE_ERROR
            }
        }
    }

    init {
        getTodayAnimes()
    }

    companion object {
        private const val FIELDS_TODAY = "broadcast,mean,start_season,status"
    }
}