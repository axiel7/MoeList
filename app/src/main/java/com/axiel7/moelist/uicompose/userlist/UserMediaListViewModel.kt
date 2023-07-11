package com.axiel7.moelist.uicompose.userlist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_LIST_SORT_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_LIST_SORT_PREFERENCE_KEY
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseMediaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserMediaListViewModel(
    listType: ListType
) : BaseMediaViewModel() {

    override val mediaType = listType.mediaType

    var listTypeStyle = listType.styleGlobalAppVariable.value

    var listSort by mutableStateOf(
        if (mediaType == MediaType.ANIME) App.animeListSort
        else App.mangaListSort
    )
        private set

    fun setSort(value: MediaSort) {
        viewModelScope.launch(Dispatchers.IO) {
            App.dataStore?.edit {
                if (mediaType == MediaType.ANIME) it[ANIME_LIST_SORT_PREFERENCE_KEY] = value.value
                else it[MANGA_LIST_SORT_PREFERENCE_KEY] = value.value
            }
            nextPage = null
            hasNextPage = false
            loadedAllPages = false
            listSort = value
        }
    }

    var openSortDialog by mutableStateOf(false)

    private val params = ApiParams(
        status = listType.status.value,
        sort = listSort.value,
        nsfw = App.nsfw,
        fields = if (mediaType == MediaType.ANIME) AnimeRepository.USER_ANIME_LIST_FIELDS
        else MangaRepository.USER_MANGA_LIST_FIELDS
    )

    var selectedItem by mutableStateOf<BaseUserMediaList<*>?>(null)
    override var myListStatus by object : MutableState<BaseMyListStatus?> {
        override var value: BaseMyListStatus?
            get() = selectedItem?.listStatus
            set(value) {
                when (value) {
                    is MyAnimeListStatus -> selectedItem =
                        (selectedItem as? UserAnimeList)?.copy(listStatus = value)

                    is MyMangaListStatus -> selectedItem =
                        (selectedItem as? UserMangaList)?.copy(listStatus = value)
                }
                onMediaItemStatusChanged(value)
            }

        override fun component1(): BaseMyListStatus? = value
        override fun component2(): (BaseMyListStatus?) -> Unit = { value = it }
    }
    val mediaList = mutableStateListOf<BaseUserMediaList<out BaseMediaNode>>()
    var nextPage: String? = null
    var hasNextPage = false
    var loadedAllPages = false
    var isLoadingList by mutableStateOf(false)

    fun getUserList(page: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = page == null
            isLoadingList = true
            params.sort = listSort.value
            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.getUserAnimeList(params, page)
            else
                MangaRepository.getUserMangaList(params, page)

            if (result?.data != null) {
                if (page == null) mediaList.clear()
                mediaList.addAll(result.data)

                nextPage = result.paging?.next
                hasNextPage = nextPage != null
                loadedAllPages = page != null && nextPage == null
            } else {
                setErrorMessage(result?.message ?: result?.error ?: "Generic error")
                hasNextPage = false
            }
            isLoadingList = false
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
            else MangaRepository.updateMangaEntry(
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
                val foundIndex = mediaList.indexOfFirst { it.node.id == mediaId }
                if (foundIndex != -1) {
                    if (mediaType == MediaType.ANIME) {
                        mediaList[foundIndex] = (mediaList[foundIndex] as UserAnimeList)
                            .copy(listStatus = result as MyAnimeListStatus)
                    } else if (mediaType == MediaType.MANGA) {
                        mediaList[foundIndex] = (mediaList[foundIndex] as UserMangaList)
                            .copy(listStatus = result as MyMangaListStatus)
                    }
                }
            }
            isLoading = false
        }
    }

    fun onItemSelected(item: BaseUserMediaList<*>) {
        selectedItem = item
        mediaInfo = item.node
    }

    private fun onMediaItemStatusChanged(myListStatus: BaseMyListStatus?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (myListStatus != null && selectedItem != null) {
                val foundIndex = mediaList.indexOfFirst { it.node.id == selectedItem!!.node.id }
                if (foundIndex != -1) {
                    if (mediaType == MediaType.ANIME) {
                        mediaList[foundIndex] = (mediaList[foundIndex] as UserAnimeList)
                            .copy(listStatus = myListStatus as MyAnimeListStatus)
                    } else if (mediaType == MediaType.MANGA) {
                        mediaList[foundIndex] = (mediaList[foundIndex] as UserMangaList)
                            .copy(listStatus = myListStatus as MyMangaListStatus)
                    }
                }
            }
        }
    }
}