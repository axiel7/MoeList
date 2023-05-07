package com.axiel7.moelist.uicompose.season

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SeasonChartViewModel: BaseViewModel() {

    var season by mutableStateOf(SeasonCalendar.currentStartSeason)
    private set

    fun setSeason(
        season: Season? = null,
        year: Int? = null
    ) {
        if (season != null && year != null) this.season = StartSeason(year, season)
        else if (season != null) this.season = this.season.copy(season = season)
        else if (year != null) this.season = this.season.copy(year = year)
    }

    val years = ((SeasonCalendar.currentYear + 1) downTo BASE_YEAR).toList()

    private val params = ApiParams(
        sort = MediaSort.ANIME_NUM_USERS.value,
        nsfw = App.nsfw,
        fields = AnimeRepository.SEASONAL_FIELDS
    )

    var animes = mutableStateListOf<AnimeSeasonal>()
    var nextPage: String? = null
    var hasNextPage = false

    fun getSeasonalAnime(page: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            if (page == null) {
                isLoading = true
                nextPage = null
                hasNextPage = false
            }
            val result = AnimeRepository.getSeasonalAnimes(
                apiParams = params,
                year = season.year,
                season = season.season,
                page = page
            )

            if (result?.data != null) {
                if (page == null) animes.clear()
                animes.addAll(result.data)

                nextPage = result.paging?.next
                hasNextPage = nextPage != null
            } else {
                setErrorMessage(result?.message ?: "Generic error")
                hasNextPage = false
            }
            isLoading = false
        }
    }

    companion object {
        const val BASE_YEAR = 1917
    }
}