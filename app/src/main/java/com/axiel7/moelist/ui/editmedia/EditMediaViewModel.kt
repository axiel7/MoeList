package com.axiel7.moelist.ui.editmedia

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditMediaViewModel(
    mediaType: MediaType,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
) : BaseViewModel<EditMediaUiState>(), EditMediaEvent {

    override val mutableUiState = MutableStateFlow(EditMediaUiState(mediaType = mediaType))

    override fun setMediaInfo(value: BaseMediaNode) {
        mutableUiState.update { it.copy(mediaInfo = value) }
    }

    override fun setEditVariables(myListStatus: BaseMyListStatus) {
        mutableUiState.update {
            it.copy(
                status = myListStatus.status,
                score = myListStatus.score,
                startDate = DateUtils.getLocalDateFromDateString(myListStatus.startDate),
                finishDate = DateUtils.getLocalDateFromDateString(myListStatus.finishDate),
                progress = myListStatus.progress ?: 0,
                volumeProgress = (myListStatus as? MyMangaListStatus)?.numVolumesRead ?: 0,
                isRepeating = myListStatus.isRepeating,
                repeatCount = myListStatus.repeatCount ?: 0,
                repeatValue = myListStatus.repeatValue ?: 0,
                priority = myListStatus.priority,
                tags = myListStatus.tags?.joinToString(),
                comments = myListStatus.notesEscaped(),
                myListStatus = myListStatus,
            )
        }
    }

    override fun onChangeStatus(value: ListStatus) {
        mutableUiState.update {
            it.copy(
                status = value,
                startDate = if (it.isNewEntry && value.isCurrent()) LocalDate.now() else it.startDate,
                finishDate = if (value == ListStatus.COMPLETED) LocalDate.now() else it.finishDate,
                progress = if (value == ListStatus.COMPLETED) {
                    it.mediaInfo?.totalDuration()?.takeIf { total -> total > 0 } ?: it.progress
                } else it.progress,
                volumeProgress = if (it.mediaInfo is MangaNode) {
                    it.mediaInfo.numVolumes?.takeIf { vols -> vols > 0 } ?: it.volumeProgress
                } else it.volumeProgress
            )
        }
    }

    override fun onChangeProgress(value: Int?) {
        mutableUiState.update {
            val progressLimit = it.mediaInfo?.totalDuration()
            if (canChangeProgressTo(value, progressLimit)) {
                val isUpdatingFromPlanning = it.status.isPlanning()

                val isUpdatingFromAutoCompleted =
                    value != null
                            && progressLimit != null
                            && it.status == ListStatus.COMPLETED
                            && value < progressLimit

                val isUpdatingToLastProgress =
                    progressLimit != null
                            && value == progressLimit
                            && it.status != ListStatus.COMPLETED

                val newStatus = when {
                    isUpdatingFromPlanning
                            || isUpdatingFromAutoCompleted -> {
                        if (it.mediaType == MediaType.ANIME) ListStatus.WATCHING else ListStatus.READING
                    }

                    isUpdatingToLastProgress -> ListStatus.COMPLETED

                    else -> it.status
                }

                it.copy(
                    progress = value,
                    status = newStatus,
                )
            } else {
                it
            }
        }
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

    override fun onChangeVolumeProgress(value: Int?) {
        mutableUiState.update {
            if (canChangeProgressTo(value, (it.mediaInfo as? MangaNode)?.numVolumes)) {
                it.copy(
                    volumeProgress = value,
                    status = if (it.status == ListStatus.PLAN_TO_READ) ListStatus.READING else it.status
                )
            } else {
                it
            }
        }
    }

    override fun onChangeScore(value: Int) {
        mutableUiState.update { it.copy(score = value) }
    }

    override fun onChangeStartDate(value: LocalDate?) {
        mutableUiState.update { it.copy(startDate = value, openStartDatePicker = false) }
    }

    override fun openStartDatePicker() {
        mutableUiState.update { it.copy(openStartDatePicker = true) }
    }

    override fun onChangeFinishDate(value: LocalDate?) {
        mutableUiState.update { it.copy(finishDate = value, openFinishDatePicker = false) }
    }

    override fun openFinishDatePicker() {
        mutableUiState.update { it.copy(openFinishDatePicker = true) }
    }

    override fun closeDatePickers() {
        mutableUiState.update {
            it.copy(openStartDatePicker = false, openFinishDatePicker = false)
        }
    }

    override fun onChangeTags(value: String) {
        mutableUiState.update { it.copy(tags = value) }
    }

    override fun onChangePriority(value: Int) {
        mutableUiState.update { it.copy(priority = value) }
    }

    override fun onChangeComments(value: String) {
        mutableUiState.update { it.copy(comments = value) }
    }

    override fun onChangeIsRepeating(value: Boolean) {
        mutableUiState.update { it.copy(isRepeating = value) }
    }

    override fun onChangeRepeatCount(value: Int?) {
        mutableUiState.update {
            it.copy(
                repeatCount = if (value != null && value >= 0 || value == null) value
                else it.repeatCount
            )
        }
    }

    override fun onChangeRepeatValue(value: Int) {
        mutableUiState.update { it.copy(repeatValue = value) }
    }

    override fun updateListItem() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.value.run {
                if (mediaInfo == null) return@launch
                setLoading(true)

                val statusValue = if (status != myListStatus?.status)
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
                val commentsValue = if (comments != myListStatus?.notesEscaped()) comments else null
                val startDateISO = startDate?.format(DateTimeFormatter.ISO_DATE)
                val startDateValue = if (startDateISO != myListStatus?.startDate)
                    startDateISO else null
                val endDateISO = finishDate?.format(DateTimeFormatter.ISO_DATE)
                val endDateValue = if (endDateISO != myListStatus?.finishDate)
                    endDateISO else null

                val result = if (mediaType == MediaType.ANIME) {
                    animeRepository.updateAnimeEntry(
                        animeId = mediaInfo.id,
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
                } else {
                    mangaRepository.updateMangaEntry(
                        mangaId = mediaInfo.id,
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
                }

                if (result != null && result.error == null) {
                    mutableUiState.update {
                        it.copy(
                            myListStatus = result,
                            updateSuccess = true,
                            isLoading = false
                        )
                    }
                } else {
                    mutableUiState.update {
                        it.copy(
                            updateSuccess = false,
                            message = result?.message,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    override fun toggleDeleteDialog(open: Boolean) {
        mutableUiState.update { it.copy(openDeleteDialog = open) }
    }

    override fun deleteEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaId = mutableUiState.value.mediaInfo?.id ?: return@launch
            setLoading(true)
            val result = if (mutableUiState.value.mediaType == MediaType.ANIME) {
                animeRepository.deleteAnimeEntry(mediaId)
            } else {
                mangaRepository.deleteMangaEntry(mediaId)
            }

            mutableUiState.update {
                it.copy(
                    myListStatus = if (result) null else it.myListStatus,
                    updateSuccess = result,
                    removed = result,
                    isLoading = false,
                    openDeleteDialog = false
                )
            }
        }
    }

    override fun onDismiss() {
        mutableUiState.update { it.copy(updateSuccess = null) }
    }
}
