package com.axiel7.moelist.ui.search

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : BaseViewModel<SearchUiState>(), SearchEvent {

    override val mutableUiState = MutableStateFlow(SearchUiState())

    override fun loadMore() {
        if (mutableUiState.value.canLoadMore) {
            mutableUiState.update { it.copy(loadMore = true) }
        }
    }

    override fun search(query: String) {
        mutableUiState.update {
            it.copy(
                query = query,
                performSearch = true,
                nextPage = null
            )
        }
    }

    override fun onChangeMediaType(value: MediaType) {
        mutableUiState.update {
            it.copy(
                mediaType = value,
                performSearch = true,
                nextPage = null
            )
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState
                .distinctUntilChanged { old, new ->
                    old.performSearch == new.performSearch
                            && old.loadMore == new.loadMore
                            && old.mediaType == new.mediaType
                            && old.query == new.query
                }
                .filter { it.query.isNotBlank() && (it.performSearch || it.loadMore) }
                .collectLatest { uiState ->
                    setLoading(uiState.nextPage == null)

                    val result = if (uiState.mediaType == MediaType.ANIME) {
                        animeRepository.searchAnime(
                            query = uiState.query,
                            limit = 25,
                            page = uiState.nextPage
                        )
                    } else {
                        mangaRepository.searchManga(
                            query = uiState.query,
                            limit = 25,
                            page = uiState.nextPage
                        )
                    }

                    if (result.data != null) {
                        if (uiState.performSearch) uiState.mediaList.clear()
                        uiState.mediaList.addAll(result.data)

                        mutableUiState.update {
                            it.copy(
                                performSearch = false,
                                noResults = result.data.isEmpty(),
                                loadMore = false,
                                nextPage = result.paging?.next,
                                isLoading = false
                            )
                        }
                    } else {
                        mutableUiState.update {
                            it.copy(
                                performSearch = false,
                                loadMore = false,
                                nextPage = null,
                                isLoading = false,
                                message = result.message
                            )
                        }
                    }
                }
        }
    }
}