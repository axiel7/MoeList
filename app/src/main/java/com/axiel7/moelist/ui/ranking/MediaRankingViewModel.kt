package com.axiel7.moelist.ui.ranking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.navigation.NavArgument
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaRankingViewModel(
    rankingType: RankingType,
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : BaseViewModel<MediaRankingUiState>(), MediaRankingEvent {

    private val mediaType = savedStateHandle
        .getStateFlow<String?>(NavArgument.MediaType.name, null)
        .filterNotNull()
        .map { MediaType.valueOf(it) }

    override val mutableUiState = MutableStateFlow(MediaRankingUiState(rankingType))

    override fun loadMore() {
        if (mutableUiState.value.canLoadMore) {
            mutableUiState.update { it.copy(loadMore = true) }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState
                .distinctUntilChanged { old, new ->
                    old.loadMore == new.loadMore
                            && old.rankingType == new.rankingType
                }
                .filter { it.loadMore }
                .combine(mediaType, ::Pair)
                .collectLatest { (uiState, mediaType) ->
                    setLoading(uiState.nextPage == null) // show indicator on first load

                    val result = if (mediaType == MediaType.ANIME) {
                        animeRepository.getAnimeRanking(
                            rankingType = uiState.rankingType,
                            limit = 25,
                            fields = AnimeRepository.RANKING_FIELDS,
                            page = uiState.nextPage,
                        )
                    } else {
                        mangaRepository.getMangaRanking(
                            rankingType = uiState.rankingType,
                            limit = 25,
                            page = uiState.nextPage,
                        )
                    }

                    if (result.data != null) {
                        if (uiState.nextPage == null) uiState.mediaList.clear()
                        uiState.mediaList.addAll(result.data)

                        mutableUiState.update {
                            it.copy(
                                nextPage = result.paging?.next,
                                loadMore = false,
                                isLoading = false,
                            )
                        }
                    } else {
                        mutableUiState.update {
                            it.copy(
                                nextPage = null,
                                loadMore = false,
                                isLoading = false,
                                message = result.message,
                            )
                        }
                    }
                }
        }
    }
}