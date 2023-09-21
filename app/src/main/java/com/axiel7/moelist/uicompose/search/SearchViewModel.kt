package com.axiel7.moelist.uicompose.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.CommonApiParams
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : BaseViewModel() {

    private val params = CommonApiParams(
        nsfw = App.nsfw,
        fields = AnimeRepository.SEARCH_FIELDS
    )
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
                AnimeRepository.searchAnime(
                    query = query,
                    commonApiParams = params,
                    page = page
                )
            else
                MangaRepository.searchManga(
                    query = query,
                    commonApiParams = params,
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