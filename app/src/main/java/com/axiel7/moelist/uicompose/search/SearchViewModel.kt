package com.axiel7.moelist.uicompose.search

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel: BaseViewModel() {

    private val params = ApiParams(
        nsfw = App.nsfw,
        fields = AnimeRepository.SEARCH_FIELDS
    )
    var nextPage: String? = null
    var hasNextPage = false

    var mediaList = mutableStateListOf<BaseMediaList>()

    fun search(
        mediaType: MediaType,
        query: String,
        page: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (page == null) {
                mediaList.clear()
                isLoading = true
            }
            params.q = query
            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.searchAnime(params, page)
            else
                MangaRepository.searchManga(params, page)

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