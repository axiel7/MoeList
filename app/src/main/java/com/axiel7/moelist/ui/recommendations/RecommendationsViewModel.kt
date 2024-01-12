package com.axiel7.moelist.ui.recommendations

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecommendationsViewModel(
    private val animeRepository: AnimeRepository
) : BaseViewModel() {

    val animes = mutableStateListOf<AnimeList>()
    private var nextPage: String? = null
    private val hasNextPage get() = nextPage != null

    fun getRecommendedAnimes(page: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        if (page == null) {
            isLoading = true
            nextPage = null
        }
        val result = animeRepository.getRecommendedAnimes(
            limit = 25,
            page = page
        )
        if (result?.data != null) {
            if (page == null) animes.clear()
            animes.addAll(result.data)
            nextPage = result.paging?.next
        } else {
            setErrorMessage(result?.message ?: result?.error ?: GENERIC_ERROR)
        }
        isLoading = false
    }

    fun loadMore() {
        if (hasNextPage && !isLoading) getRecommendedAnimes(nextPage)
    }
}