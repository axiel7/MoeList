package com.axiel7.moelist.uicompose.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.BaseMediaDetails
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.Related
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaDetailsViewModel(
    val mediaType: MediaType
): BaseViewModel() {

    init {
        isLoading = true
    }

    var mediaDetails by mutableStateOf<BaseMediaDetails?>(null)
    var studioSerializationJoined by mutableStateOf<String?>(null)
    var related = mutableStateListOf<Related>()

    fun getDetails(mediaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            if (mediaType == MediaType.ANIME) {
                mediaDetails = AnimeRepository.getAnimeDetails(mediaId)
                studioSerializationJoined = (mediaDetails as? AnimeDetails?)?.studios?.joinToString { it.name }
            } else {
                mediaDetails = MangaRepository.getMangaDetails(mediaId)
                studioSerializationJoined = (mediaDetails as? MangaDetails?)?.serialization?.joinToString { it.node.name }
            }
            related.clear()
            mediaDetails?.relatedAnime?.let { related.addAll(it) }
            mediaDetails?.relatedManga?.let { related.addAll(it) }
            isLoading = false
        }
    }

    var status by mutableStateOf(if (mediaType == MediaType.ANIME) ListStatus.PTW else ListStatus.PTR)
    var progress by mutableStateOf(0)
    var volumeProgress by mutableStateOf(0)
    var score by mutableStateOf(0)
    var startDate by mutableStateOf<String?>(null)
    var endDate by mutableStateOf<String?>(null)
    var repeatCount by mutableStateOf(0)

    private fun setEditVariables(myListStatus: BaseMyListStatus) {
        status = myListStatus.status
        score = myListStatus.score
        startDate = myListStatus.startDate
        endDate = myListStatus.endDate
        if (mediaType == MediaType.ANIME) {
            (myListStatus as? MyAnimeListStatus).apply {
                progress = this?.numEpisodesWatched ?: 0
                repeatCount = this?.numTimesRewatched ?: 0
            }
        } else {
            (myListStatus as? MyMangaListStatus).apply {
                progress = this?.numChaptersRead ?: 0
                volumeProgress = this?.numVolumesRead ?: 0
                repeatCount = this?.numTimesReread ?: 0
            }
        }
    }

    fun onChangeProgress(value: Int?): Boolean {
        if (value != null && value >= 0) {
            val topLimit = if (mediaType == MediaType.ANIME) animeDetails?.numEpisodes
            else mangaDetails?.numChapters

            if (topLimit == null || topLimit <= 0) {
                progress = value
                return true
            }
            else if (value <= topLimit) {
                progress = value
                return true
            }
        }
        return false
    }

    fun onChangeVolumeProgress(value: Int?): Boolean {
        if (value != null && value >= 0) {
            val topLimit = mangaDetails?.numVolumes

            if (topLimit == null || topLimit <= 0) {
                volumeProgress = value
                return true
            }
            else if (value <= topLimit) {
                volumeProgress = value
                return true
            }
        }
        return false
    }

    fun onChangeRepeatCount(value: Int?): Boolean {
        if (value != null && value >= 0) {
            repeatCount = value
            return true
        }
        return false
    }

    var openDatePicker by mutableStateOf(false)

    fun buildQueryFromThemeText(themeText: String): String {
        var query = themeText.replace(" ", "+")
        val size = query.length

        if (query.startsWith("#")) {
            query = query.substring(4, size)
        }
        val index = query.indexOf("(ep")

        return if (index == -1) query
        else query.substring(0, index - 1)
    }
}