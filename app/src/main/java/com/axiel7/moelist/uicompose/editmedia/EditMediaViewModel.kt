package com.axiel7.moelist.uicompose.editmedia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
) : BaseMediaViewModel() {

    override var myListStatus: BaseMyListStatus? = null

    var status by mutableStateOf(if (mediaType == MediaType.ANIME) ListStatus.PTW else ListStatus.PTR)
    var progress by mutableStateOf<Int?>(0)
    var volumeProgress by mutableStateOf<Int?>(0)
    var score by mutableIntStateOf(0)
    var startDate by mutableStateOf<LocalDate?>(null)
    var finishDate by mutableStateOf<LocalDate?>(null)
    var isRepeating by mutableStateOf(false)
    var repeatCount by mutableIntStateOf(0)
    var repeatValue by mutableIntStateOf(0)
    var priority by mutableIntStateOf(0)
    var tags by mutableStateOf<String?>(null)
    var comments by mutableStateOf<String?>(null)

    fun setEditVariables(myListStatus: BaseMyListStatus) {
        this.myListStatus = myListStatus
        status = myListStatus.status
        score = myListStatus.score
        startDate = DateUtils.getLocalDateFromDateString(myListStatus.startDate)
        finishDate = DateUtils.getLocalDateFromDateString(myListStatus.finishDate)

        progress = myListStatus.progress ?: 0
        (myListStatus as? MyMangaListStatus)?.numVolumesRead?.let { volumeProgress = it }
        isRepeating = myListStatus.isRepeating
        repeatCount = myListStatus.repeatCount ?: 0
        repeatValue = myListStatus.repeatValue ?: 0
        priority = myListStatus.priority
        tags = myListStatus.tags?.joinToString()
        comments = myListStatus.comments
    }

    fun onChangeStatus(value: ListStatus, isNewEntry: Boolean = false) {
        status = value
        if (isNewEntry && value.isCurrent()) {
            startDate = LocalDate.now()
        } else if (value == ListStatus.COMPLETED) {
            finishDate = LocalDate.now()
            mediaInfo?.totalDuration()?.let { if (it > 0) progress = it }
            if (mediaInfo is MangaNode) {
                (mediaInfo as? MangaNode)?.numVolumes?.let { if (it > 0) volumeProgress = it }
            }
        }
    }

    fun onChangeProgress(value: Int?) {
        if (canChangeProgressTo(value, progressLimit)) {
            progress = value
            when {
                status.isPlanning()
                        || (value != null
                        && progressLimit != null
                        && status == ListStatus.COMPLETED
                        && value < progressLimit)
                -> status =
                    if (mediaType == MediaType.ANIME) ListStatus.WATCHING else ListStatus.READING

                value != null
                        && progressLimit != null
                        && value == progressLimit
                        && status != ListStatus.COMPLETED -> status = ListStatus.COMPLETED
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
    var selectedDateType by mutableIntStateOf(-1)

    var updateSuccess by mutableStateOf(false)

    fun updateListItem() {
        viewModelScope.launch(Dispatchers.IO) {
            if (mediaInfo == null) return@launch
            isLoading = true

            val statusValue = if (status.value != myListStatus?.status?.value)
                status else null
            val scoreValue = if (score != myListStatus?.score)
                score else null
            val progressValue = if (progress != myListStatus?.progress)
                progress else null
            val volumeProgressValue =
                if (volumeProgress != (myListStatus as? MyMangaListStatus)?.numVolumesRead)
                    volumeProgress else null
            val isRepeatingValue = if (isRepeating != myListStatus?.isRepeating)
                isRepeating else null
            val repeatCountValue = if (repeatCount != myListStatus?.repeatCount)
                repeatCount else null
            val repeatValueValue = if (repeatValue != myListStatus?.repeatValue)
                repeatValue else null
            val priorityValue = if (priority != myListStatus?.priority) priority else null
            val tagsValue = if (tags != myListStatus?.tags?.joinToString()) tags else null
            val commentsValue = if (comments != myListStatus?.comments) comments else null
            val startDateISO = startDate?.format(DateTimeFormatter.ISO_DATE)
            val startDateValue = if (startDateISO != myListStatus?.startDate)
                startDateISO else null
            val endDateISO = finishDate?.format(DateTimeFormatter.ISO_DATE)
            val endDateValue = if (endDateISO != myListStatus?.finishDate)
                endDateISO else null

            val result = if (mediaType == MediaType.ANIME)
                AnimeRepository.updateAnimeEntry(
                    animeId = mediaInfo!!.id,
                    status = statusValue,
                    score = scoreValue,
                    watchedEpisodes = progressValue,
                    startDate = startDateValue,
                    endDate = endDateValue,
                    isRewatching = isRepeatingValue,
                    numRewatches = repeatCountValue,
                    rewatchValue = repeatValueValue,
                    priority = priorityValue,
                    tags = tagsValue,
                    comments = commentsValue
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
                    isRereading = isRepeatingValue,
                    numRereads = repeatCountValue,
                    rereadValue = repeatValueValue,
                    priority = priorityValue,
                    tags = tagsValue,
                    comments = commentsValue
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