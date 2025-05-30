package com.axiel7.moelist.ui.season

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.SeasonType
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SeasonChartViewModel(
    private val animeRepository: AnimeRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<SeasonChartUiState>(), SeasonChartEvent {

    override val mutableUiState = MutableStateFlow(SeasonChartUiState())

    override fun loadMore() {
        if (mutableUiState.value.canLoadMore) {
            mutableUiState.update { it.copy(loadMore = true) }
        }
    }

    override fun setSeason(season: Season?, year: Int?) {
        mutableUiState.update { uiState ->
            val startSeason = when {
                season != null && year != null -> StartSeason(year, season)
                season != null -> uiState.season.copy(season = season)
                year != null -> uiState.season.copy(year = year)
                else -> uiState.season
            }
            uiState.copy(
                season = startSeason,
                seasonType = SeasonType.entries.find { it.season == startSeason }
            )
        }
    }

    override fun setSeason(type: SeasonType) {
        mutableUiState.update {
            it.copy(
                season = type.season,
                seasonType = type
            )
        }
    }

    override fun onChangeSort(value: MediaSort) {
        mutableUiState.update { it.copy(sort = value) }
    }

    override fun onChangeIsNew(value: Boolean) {
        mutableUiState.update { it.copy(isNew = value) }
    }

    override fun onApplyFilters() {
        mutableUiState.update { it.copy(loadMore = true, nextPage = null) }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState
                .distinctUntilChanged { old, new ->
                    old.loadMore == new.loadMore
                            && old.season == new.season
                            && old.sort == new.sort
                }
                .filter { it.loadMore }
                .collectLatest { uiState ->
                    setLoading(uiState.nextPage == null)

                    val result = animeRepository.getSeasonalAnimes(
                        sort = uiState.sort,
                        startSeason = uiState.season,
                        isNew = uiState.isNew,
                        limit = 25,
                        fields = AnimeRepository.SEASONAL_FIELDS,
                        page = uiState.nextPage,
                    )

                    if (result.data != null) {
                        if (uiState.nextPage == null) uiState.animes.clear()
                        uiState.animes.addAll(result.data)

                        mutableUiState.update {
                            it.copy(
                                nextPage = result.paging?.next,
                                loadMore = false,
                                isLoading = false
                            )
                        }
                    } else {
                        mutableUiState.update {
                            it.copy(
                                nextPage = null,
                                loadMore = false,
                                isLoading = false,
                                message = result.message
                            )
                        }
                    }
                }
        }

        defaultPreferencesRepository.hideScores
            .onEach { value ->
                mutableUiState.update { it.copy(hideScore = value) }
            }
            .launchIn(viewModelScope)
    }
}