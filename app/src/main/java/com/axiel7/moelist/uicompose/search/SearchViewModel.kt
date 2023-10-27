package com.axiel7.moelist.uicompose.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : BaseViewModel() {

    var nextPage: String? = null
    var hasNextPage = false

    var mediaType by mutableStateOf(MediaType.ANIME)

    val mediaList = mutableStateListOf<BaseMediaList>()

    fun search(
        query: String,
        page: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (page == null) {
                mediaList.clear()
                isLoading = true
            }
            val result = if (mediaType == MediaType.ANIME)
                animeRepository.searchAnime(
                    query = query,
                    limit = 25,
                    offset = null,
                    page = page
                )
            else
                mangaRepository.searchManga(
                    query = query,
                    limit = 25,
                    offset = null,
                    page = page
                )

            if (result?.data != null) {
                mediaList.addAll(result.data)

                nextPage = result.paging?.next
                hasNextPage = nextPage != null
            } else {
                setErrorMessage(result?.message ?: "Generic error")
                hasNextPage = false
            }
            isLoading = false
        }
    }
}