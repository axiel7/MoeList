package com.axiel7.moelist.uicompose.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.isCurrent
import com.axiel7.moelist.data.model.media.isPlanning
import com.axiel7.moelist.data.model.media.totalDuration
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.uicompose.base.BaseMediaViewModel
import com.axiel7.moelist.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditMediaViewModel(
    override val mediaType: MediaType,
    override var mediaInfo: BaseMediaNode?
): BaseMediaViewModel() {

    override var myListStatus: BaseMyListStatus? = null

    var status by mutableStateOf(if (mediaType == MediaType.ANIME) ListStatus.PTW else ListStatus.PTR)
    var progress by mutableStateOf<Int?>(0)
    var volumeProgress by mutableStateOf<Int?>(0)
    var score by mutableStateOf(0)
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var repeatCount by mutableStateOf(0)

    fun setEditVariables(myListStatus: BaseMyListStatus) {
        this.myListStatus = myListStatus
        status = myListStatus.status
        score = myListStatus.score
        myListStatus.startDate?.let { startDate = DateUtils.getLocalDateFromDateString(it) }
        myListStatus.endDate?.let { endDate = DateUtils.getLocalDateFromDateString(it) }

        progress = myListStatus.progress ?: 0
        (myListStatus as? MyMangaListStatus)?.numVolumesRead?.let { volumeProgress = it }
        repeatCount = myListStatus.repeatCount ?: 0
    }

    fun onChangeStatus(value: ListStatus, isNewEntry: Boolean = false) {
        status = value
        if (isNewEntry && value.isCurrent()) {
            startDate = LocalDate.now()
        }
        else if (value == ListStatus.COMPLETED) {
            endDate = LocalDate.now()
            mediaInfo?.totalDuration()?.let { if (it > 0) progress = it }
            if (mediaInfo is MangaNode) {
                (mediaInfo as? MangaNode)?.numVolumes?.let { if (it > 0) volumeProgress = it }
            }
        }
    }

    fun onChangeProgress(value: Int?) {
        if (canChangeProgressTo(value, progressLimit)) {
            progress = value
            if (status.isPlanning()) {
                status = if (mediaType == MediaType.ANIME) ListStatus.WATCHING else ListStatus.READING
            }
        }
    }

    private val progressLimit = when (mediaInfo) {
        is AnimeNode -> (mediaInfo as AnimeNode).numEpisodes
        is MangaNode -> (mediaInfo as MangaNode).numChapters
        else -> null
    }

    private fun canChangeProgressTo(value: Int?, limit: Int?) = when {
        value == null -> true //allow to set empty
        value < 0 -> false //progress must be positive
        value == 0 -> true //allow set to 0
        limit == null -> true //no limitations
        limit <= 0 -> true //no limitations
        value <= limit -> true //progress must be below total
        else -> false
    }

    fun onChangeVolumeProgress(value: Int?) {
        if (canChangeProgressTo(value, (mediaInfo as? MangaNode)?.numVolumes)) {
            volumeProgress = value
            if (status == ListStatus.PTR) {
                status = ListStatus.READING
            }
        }
    }

    fun onChangeRepeatCount(value: Int?): Boolean {
        if (value != null && value >= 0) {
            repeatCount = value
            return true
        }
        return false
    }

    var openDatePicker by mutableStateOf(false)
    var selectedDateType by mutableStateOf(-1)

    var updateSuccess by mutableStateOf(false)

    fun updateListItem() {
        viewModelScope.launch(Dispatchers.IO) {
            if (mediaInfo == null) return@launch
            isLoading = true

            val statusValue = if (status.value != myListStatus?.status?.value)
                status.value else null
            val scoreValue = if (score != myListStatus?.score)
                score else null
            val progressValue = if (progress != myListStatus?.progress)
                progress else null
            val volumeProgressValue = if (volumeProgress != (myListStatus as? MyMangaListStatus)?.numVolumesRead)
                volumeProgress else null
            val repeatCountValue = if (repeatCount != myListStatus?.repeatCount)
                repeatCount else null
            val startDateISO = startDate?.format(DateTimeFormatter.ISO_DATE)
            val startDateValue = if (startDateISO != myListStatus?.startDate)
                startDateISO else null
            val endDateISO = endDate?.format(DateTimeFormatter.ISO_DATE)
            val endDateValue = if (endDateISO != myListStatus?.endDate)
                endDateISO else null

            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.updateAnimeEntry(
                    animeId = mediaInfo!!.id,
                    status = statusValue,
                    score = scoreValue,
                    watchedEpisodes = progressValue,
                    startDate = startDateValue,
                    endDate = endDateValue,
                    numRewatches = repeatCountValue
                )
            else
                MangaRepository.updateMangaEntry(
                    mangaId = mediaInfo!!.id,
                    status = statusValue,
                    score = scoreValue,
                    chaptersRead = progressValue,
                    volumesRead = volumeProgressValue,
                    startDate = startDateValue,
                    endDate = endDateValue,
                    numRereads = repeatCountValue
                )

            if (result != null && result.error == null) {
                myListStatus = result
                updateSuccess = true
            } else {
                updateSuccess = false
                result?.message?.let { setErrorMessage(it) }
            }

            isLoading = false
        }
    }

    var openDeleteDialog by mutableStateOf(false)

    fun deleteEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            if (mediaInfo == null) return@launch
            isLoading = true
            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.deleteAnimeEntry(mediaInfo!!.id)
            else
                MangaRepository.deleteMangaEntry(mediaInfo!!.id)

            if (result) myListStatus = null
            updateSuccess = result
            isLoading = false
        }
    }
}