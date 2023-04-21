package com.axiel7.moelist.uicompose.userlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
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

    var animeList = mutableStateListOf<UserAnimeList>()
    var mangaList = mutableStateListOf<UserMangaList>()
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
                        if (page == null) animeList.clear()
                        animeList.addAll(this)
                    }
                } else if (result.data.any { it.node is MangaNode }) {
                    (result.data as List<UserMangaList>).apply {
                        if (page == null) mangaList.clear()
                        mangaList.addAll(this)
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

    fun updateListItem(
        mediaId: Int,
        status: String? = null,
        score: Int? = null,
        progress: Int? = null,
        volumeProgress: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        repeatCount: Int? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.updateAnimeEntry(
                    animeId = mediaId,
                    status = status,
                    score = score,
                    watchedEpisodes = progress,
                    startDate = startDate,
                    endDate = endDate,
                    numRewatches = repeatCount
                )
            else MangaRepository.updateAnimeEntry(
                mangaId = mediaId,
                status = status,
                score = score,
                chaptersRead = progress,
                volumesRead = volumeProgress,
                startDate = startDate,
                endDate = endDate,
                numRereads = repeatCount
            )

            if (result != null) {
                if (mediaType == MediaType.ANIME) {
                    val foundIndex = animeList.indexOfFirst { it.node.id == mediaId }
                    if (foundIndex != -1) animeList[foundIndex] = animeList[foundIndex].copy(listStatus = result as MyAnimeListStatus)
                } else {
                    val foundIndex = mangaList.indexOfFirst { it.node.id == mediaId }
                    if (foundIndex != -1) mangaList[foundIndex] = mangaList[foundIndex].copy(listStatus = result as MyMangaListStatus)
                }
            }
            isLoading = false
        }
    }
}