package com.axiel7.moelist.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.paging.UserMangaListPaging
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.RESPONSE_NONE
import com.axiel7.moelist.utils.Constants.RESPONSE_OK
import com.axiel7.moelist.utils.Constants.SORT_MANGA_TITLE
import com.axiel7.moelist.utils.Constants.SORT_SCORE
import com.axiel7.moelist.utils.Constants.SORT_UPDATED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MangaListViewModel : ViewModel() {

    private val status = MutableStateFlow<String?>(null)
    fun setStatus(value: String) {
        status.value = when (value) {
            "all" -> null
            else -> value
        }
        params.value.status = status.value
    }

    private val sortMode = MutableStateFlow(SORT_MANGA_TITLE)
    fun setSortMode(value: String) {
        sortMode.value = value
        params.value.sort = sortMode.value
    }

    private val nsfw = MutableStateFlow(0)
    fun setNsfw(value: Int) {
        nsfw.value = value
        params.value.nsfw = nsfw.value
    }

    private val params = MutableStateFlow(
        ApiParams(
            status = status.value,
            sort = sortMode.value,
            nsfw = nsfw.value,
            fields = FIELDS
        )
    )

    var mangaListFlow = createMangaListFlow()

    private fun createMangaListFlow() = Pager(
        PagingConfig(pageSize = 15, prefetchDistance = 10)
    ) {
        UserMangaListPaging(App.api, params.value)
    }.flow
        .cachedIn(viewModelScope)

    fun updateMangaListFlow() {
        mangaListFlow = createMangaListFlow()
    }

    private val _updateResponse = MutableStateFlow<Pair<MyMangaListStatus?, String>>(null to RESPONSE_NONE)
    val updateResponse: StateFlow<Pair<MyMangaListStatus?, String>> = _updateResponse

    fun updateList(
        mangaId: Int,
        status: String? = null,
        score: Int? = null,
        chaptersRead: Int? = null,
        volumesRead: Int? = null
    ) {
        viewModelScope.launch {
            val call = async { App.api.updateUserMangaList(mangaId, status, score, chaptersRead, volumesRead) }
            val result = try {
                call.await()
            } catch (e: Exception) {
                null
            }
            if (result != null) {
                _updateResponse.value = result to RESPONSE_OK
            }
            else {
                _updateResponse.value = null to RESPONSE_ERROR
            }

        }
    }

    fun deleteEntry(mangaId: Int) {
        viewModelScope.launch {
            val call = async { App.api.deleteMangaEntry(mangaId) }
            call.await()
        }
    }

    fun getPositionFromSort(
        sort: String
    ) : Int = when (sort) {
        SORT_MANGA_TITLE -> 0
        SORT_SCORE -> 1
        SORT_UPDATED -> 2
        else -> 0
    }

    fun getSortFromPosition(
        position: Int
    ) : String = when (position) {
        0 -> SORT_MANGA_TITLE
        1 -> SORT_SCORE
        2 -> SORT_UPDATED
        else -> SORT_MANGA_TITLE
    }

    companion object {
        private const val FIELDS = "list_status,num_chapters,media_type,status"
    }
}