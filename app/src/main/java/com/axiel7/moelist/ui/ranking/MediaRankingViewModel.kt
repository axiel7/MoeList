package com.axiel7.moelist.ui.ranking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

class MediaRankingViewModel(
    rankingType: RankingType,
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : BaseViewModel<MediaRankingUiState>(), MediaRankingEvent {

    private val args = savedStateHandle.toRoute<Route.MediaRanking>(
        typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
    )
    private val mediaType = args.mediaType

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
                .collectLatest { uiState ->
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
