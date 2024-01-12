package com.axiel7.moelist.ui.season

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.ui.base.BaseViewModel
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SeasonChartViewModel(
    private val animeRepository: AnimeRepository
) : BaseViewModel() {

    var season by mutableStateOf(SeasonCalendar.currentStartSeason)
        private set

    fun setSeason(
        season: Season? = null,
        year: Int? = null
    ) {
        when {
            season != null && year != null -> this.season = StartSeason(year, season)
            season != null -> this.season = this.season.copy(season = season)
            year != null -> this.season = this.season.copy(year = year)
        }
    }

    val years = ((SeasonCalendar.currentYear + 1) downTo BASE_YEAR).toList()

    var sort by mutableStateOf(MediaSort.ANIME_NUM_USERS)
        private set

    fun onChangeSort(value: MediaSort) {
        sort = value
    }

    val animes = mutableStateListOf<AnimeSeasonal>()
    var nextPage: String? = null
    var hasNextPage by mutableStateOf(false)

    fun getSeasonalAnime(page: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        if (page == null) {
            isLoading = true
            nextPage = null
            hasNextPage = false
        }
        val result = animeRepository.getSeasonalAnimes(
            sort = sort,
            year = season.year,
            season = season.season,
            limit = 25,
            fields = AnimeRepository.SEASONAL_FIELDS,
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

    companion object {
        const val BASE_YEAR = 1917
    }
}