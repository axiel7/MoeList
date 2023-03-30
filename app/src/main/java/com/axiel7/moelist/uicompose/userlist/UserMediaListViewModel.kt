package com.axiel7.moelist.uicompose.userlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserMediaListViewModel(
    val mediaType: MediaType,
    listStatus: ListStatus
): BaseViewModel() {

    var listSort by mutableStateOf(
        if (mediaType == MediaType.ANIME) Constants.SORT_ANIME_TITLE
        else Constants.SORT_MANGA_TITLE
    )
    private set
    fun setSort(value: String) {
        listSort = value
        params.sort = value
    }

    private val params = ApiParams(
        status = listStatus.value,
        sort = listSort,
        nsfw = nsfw,
        fields = if (mediaType == MediaType.ANIME) AnimeRepository.USER_ANIME_LIST_FIELDS
        else MangaRepository.USER_MANGA_LIST_FIELDS
    )

    var animeList by mutableStateOf(emptyList<UserAnimeList>())
    var mangaList by mutableStateOf(emptyList<UserMangaList>())
    var nextPage: String? = null
    var hasNextPage = false

    @Suppress("UNCHECKED_CAST")
    fun getUserList(page: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = page == null //show indicator on 1st load
            val result = if (mediaType == MediaType.ANIME) AnimeRepository.getUserAnimeList(params, page)
            else MangaRepository.getUserMangaList(params, page)

            if (result?.data != null) {
                if (result.data.any { it.node is AnimeNode }) {
                    (result.data as List<UserAnimeList>).apply {
                        animeList = if (page == null) this else animeList + this
                    }
                } else if (result.data.any { it.node is MangaNode }) {
                    (result.data as List<UserMangaList>).apply {
                        mangaList = if (page == null) this else mangaList + this
                    }
                }
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