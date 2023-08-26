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
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseMediaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserMediaListViewModel(
    override val mediaType: MediaType,
    initialListStatus: ListStatus? = null,
) : BaseMediaViewModel() {

    var listStatus by mutableStateOf(
        initialListStatus ?: if (mediaType == MediaType.ANIME) ListStatus.WATCHING
        else ListStatus.READING
    )
        private set

    fun onStatusChanged(status: ListStatus) {
        listStatus = status
        params.status = status.value
        refreshList()
    }

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
            listSort = value
            params.sort = listSort.value
            refreshList()
        }
    }

    var openSortDialog by mutableStateOf(false)
    var openSetAtCompletedDialog by mutableStateOf(false)
    var lastItemUpdatedId = 0

    private val params = ApiParams(
        status = listStatus.value,
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
    private var nextPage: String? = null
    private var hasNextPage = true
    var isLoadingList by mutableStateOf(false)

    private fun getUserList(page: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = page == null
            isLoadingList = true
            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.getUserAnimeList(params, page)
            else
                MangaRepository.getUserMangaList(params, page)

            if (result?.data != null) {
                if (page == null) mediaList.clear()
                mediaList.addAll(result.data)

                nextPage = result.paging?.next
                hasNextPage = nextPage != null
            } else {
                setErrorMessage(result?.message ?: result?.error ?: "Generic error")
                hasNextPage = false
            }
            isLoadingList = false
            isLoading = false
        }
    }

    fun refreshList() {
        nextPage = null
        hasNextPage = false
        getUserList(page = null)
    }

    fun onLoadMore() {
        if (!isLoadingList && hasNextPage) {
            getUserList(nextPage)
        }
    }

    fun updateProgress(
        mediaId: Int,
        progress: Int? = null,
        volumeProgress: Int? = null,
        totalProgress: Int?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.updateAnimeEntry(
                    animeId = mediaId,
                    watchedEpisodes = progress,
                )
            else MangaRepository.updateMangaEntry(
                mangaId = mediaId,
                chaptersRead = progress,
                volumesRead = volumeProgress,
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
                    if (totalProgress != null &&
                        (progress == totalProgress || volumeProgress == totalProgress)
                    ) {
                        lastItemUpdatedId = mediaId
                        openSetAtCompletedDialog = true
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

    fun setAsCompleted(mediaId: Int) = viewModelScope.launch(Dispatchers.IO) {
        isLoading = true
        val result = if (mediaType == MediaType.ANIME)
            AnimeRepository.updateAnimeEntry(
                animeId = mediaId,
                status = ListStatus.COMPLETED.value
            )
        else MangaRepository.updateMangaEntry(
            mangaId = mediaId,
            status = ListStatus.COMPLETED.value
        )

        if (result != null) {
            mediaList.removeIf { it.node.id == mediaId }
        }
        isLoading = false
    }
}