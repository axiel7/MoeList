package com.axiel7.moelist.ui.seasonal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.paging.AnimeSeasonalPaging
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SeasonalViewModel : ViewModel() {

    private val nsfw = MutableStateFlow(0)
    fun setNsfw(value: Int) {
        nsfw.value = value
        params.value.nsfw = nsfw.value
    }

    private val _startSeason = MutableStateFlow(
        StartSeason(
            year = SeasonCalendar.currentYear,
            season = SeasonCalendar.currentSeasonStr
        )
    )
    val startSeason: StateFlow<StartSeason> = _startSeason
    fun setStartSeason(year: Int, season: String) {
        _startSeason.value.year = year
        _startSeason.value.season = season
    }

    val params = MutableStateFlow(
        ApiParams(
            sort = Constants.SORT_ANIME_SCORE,
            nsfw = nsfw.value,
            fields = FIELDS
        )
    )

    var animeSeasonalFlow = createAnimeSeasonalFlow()

    private fun createAnimeSeasonalFlow() = Pager(
            PagingConfig(pageSize = 15, prefetchDistance = 5)
        ) {
            AnimeSeasonalPaging(
                api = App.api,
                apiParams = params.value,
                startSeason = _startSeason.value
            )
        }.flow
            .cachedIn(viewModelScope)

    fun updateAnimeSeasonalFlow() {
        animeSeasonalFlow = createAnimeSeasonalFlow()
    }

    companion object {
        private const val FIELDS = "start_season,broadcast,num_episodes,media_type,mean"
    }
}