package com.axiel7.moelist.uicompose.userlist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseMediaViewModel
import com.axiel7.moelist.uicompose.base.ItemsPerRow
import com.axiel7.moelist.uicompose.base.ListStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserMediaListViewModel(
    initialListStatus: ListStatus? = null,
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseMediaViewModel(savedStateHandle) {

    var listStatus by mutableStateOf(
        initialListStatus ?: if (mediaType == MediaType.ANIME) ListStatus.WATCHING
        else ListStatus.READING
    )
        private set

    fun onStatusChanged(status: ListStatus) {
        listStatus = status
        refreshList()
    }

    val listSort = when (mediaType) {
        MediaType.ANIME -> defaultPreferencesRepository.animeListSort
        MediaType.MANGA -> defaultPreferencesRepository.mangaListSort
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setSort(value: MediaSort) {
        viewModelScope.launch(Dispatchers.IO) {
            when (mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.setAnimeListSort(value)
                MediaType.MANGA -> defaultPreferencesRepository.setMangaListSort(value)
            }
            refreshList()
        }
    }

    val showRandomButton = defaultPreferencesRepository.randomListEntryEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val itemsPerRow = defaultPreferencesRepository.gridItemsPerRow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ItemsPerRow.DEFAULT)

    var listStyle by mutableStateOf(ListStyle.STANDARD)
        private set

    var openSortDialog by mutableStateOf(false)
    var openSetAtCompletedDialog by mutableStateOf(false)
    var lastItemUpdatedId = 0

    var selectedItem by mutableStateOf<BaseUserMediaList<*>?>(null)
    override var _myListStatus by object : MutableState<BaseMyListStatus?> {
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

    private fun getUserList(page: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        if (listSort.value == null) return@launch
        isLoading = page == null
        isLoadingList = true
        val result = if (mediaType == MediaType.ANIME)
            animeRepository.getUserAnimeList(
                status = listStatus,
                sort = listSort.value!!,
                page = page
            )
        else
            mangaRepository.getUserMangaList(
                status = listStatus,
                sort = listSort.value!!,
                page = page
            )

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

    fun onUpdateProgress(item: BaseUserMediaList<out BaseMediaNode>) {
        val isVolumeProgress = (item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() == true
        updateProgress(
            mediaId = item.node.id,
            progress = if (!isVolumeProgress) item.listStatus?.progress?.plus(1) else null,
            volumeProgress = if (isVolumeProgress) (item.listStatus as? MyMangaListStatus)
                ?.numVolumesRead?.plus(1) else null,
            totalProgress = item.totalProgress()
        )
    }

    private fun updateProgress(
        mediaId: Int,
        progress: Int? = null,
        volumeProgress: Int? = null,
        totalProgress: Int?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val result = if (mediaType == MediaType.ANIME)
                animeRepository.updateAnimeEntry(
                    animeId = mediaId,
                    watchedEpisodes = progress,
                )
            else
                mangaRepository.updateMangaEntry(
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
        _mediaInfo = item.node
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
            animeRepository.updateAnimeEntry(
                animeId = mediaId,
                status = ListStatus.COMPLETED
            )
        else
            mangaRepository.updateMangaEntry(
                mangaId = mediaId,
                status = ListStatus.COMPLETED
            )

        if (result != null) {
            mediaList.removeIf { it.node.id == mediaId }
        }
        isLoading = false
    }

    var isLoadingRandom by mutableStateOf(false)
    var randomId by mutableStateOf<Int?>(null)

    fun getRandomIdOfList() = viewModelScope.launch(Dispatchers.IO) {
        isLoadingRandom = true
        val result = if (mediaType == MediaType.ANIME)
            animeRepository.getAnimeIdsOfUserList(status = listStatus)
        else
            mangaRepository.getMangaIdsOfUserList(status = listStatus)
        isLoadingRandom = false
        if (result != null) {
            randomId = result.data?.random()
        }
    }

    init {
        combine(
            defaultPreferencesRepository.useGeneralListStyle,
            defaultPreferencesRepository.generalListStyle
        ) { useGeneral, generalStyle ->
            if (useGeneral) {
                listStyle = generalStyle
            } else {
                ListType(listStatus, mediaType)
                    .stylePreference(defaultPreferencesRepository)
                    .collect {
                        listStyle = it
                    }
            }
        }
            .launchIn(viewModelScope)
    }
}