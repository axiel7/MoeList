package com.axiel7.moelist.ui.recommendations

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecommendationsViewModel(
    private val animeRepository: AnimeRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<RecommendationsUiState>(), RecommendationsEvent {

    override val mutableUiState = MutableStateFlow(RecommendationsUiState())

    override fun loadMore() {
        if (mutableUiState.value.canLoadMore) {
            mutableUiState.update { it.copy(loadMore = true) }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState
                .distinctUntilChangedBy { it.loadMore }
                .filter { it.loadMore }
                .collectLatest { uiState ->
                    setLoading(uiState.nextPage == null)
                    val result = animeRepository.getRecommendedAnimes(
                        limit = 25,
                        page = uiState.nextPage
                    )
                    if (result.data != null) {
                        if (uiState.nextPage == null) uiState.animes.clear()
                        uiState.animes.addAll(result.data)
                        mutableUiState.update {
                            it.copy(
                                nextPage = result.paging?.next,
                                isLoading = false
                            )
                        }
                    } else {
                        mutableUiState.update {
                            it.copy(
                                isLoading = false,
                                message = result.message ?: result.error
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