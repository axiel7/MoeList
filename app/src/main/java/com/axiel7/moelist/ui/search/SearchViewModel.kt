package com.axiel7.moelist.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axiel7.moelist.data.model.SearchHistory
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.data.repository.SearchHistoryRepository
import com.axiel7.moelist.ui.base.navigation.Route
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
import kotlin.reflect.typeOf

class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SearchUiState>(), SearchEvent {

    private val args = savedStateHandle.toRoute<Route.Search>(
        typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
    )
    private val mediaType = args.mediaType

    override val mutableUiState = MutableStateFlow(SearchUiState(mediaType = mediaType))

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
        onSaveSearchHistory(query)
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

    override fun onSaveSearchHistory(query: String) {
        viewModelScope.launch {
            searchHistoryRepository.addItem(query)
        }
    }

    override fun onRemoveSearchHistory(item: SearchHistory) {
        viewModelScope.launch {
            searchHistoryRepository.deleteItem(item)
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

        viewModelScope.launch {
            searchHistoryRepository.getItems().collect { searchHistoryList ->
                mutableUiState.update {
                    it.copy(searchHistoryList = searchHistoryList)
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
