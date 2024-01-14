package com.axiel7.moelist.ui.editmedia

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.state.UiState
import java.time.LocalDate

data class EditMediaUiState(
    val mediaType: MediaType,
    val status: ListStatus = if (mediaType == MediaType.ANIME) ListStatus.PLAN_TO_WATCH else ListStatus.PLAN_TO_READ,
    val progress: Int? = null,
    val volumeProgress: Int? = null,
    val score: Int = 0,
    val startDate: LocalDate? = null,
    val finishDate: LocalDate? = null,
    val isRepeating: Boolean = false,
    val repeatCount: Int? = null,
    val repeatValue: Int = 0,
    val priority: Int = 0,
    val tags: String? = null,
    val comments: String? = null,
    val openStartDatePicker: Boolean = false,
    val openFinishDatePicker: Boolean = false,
    val openDeleteDialog: Boolean = false,
    val updateSuccess: Boolean? = null,
    val removed: Boolean = false,
    val mediaInfo: BaseMediaNode? = null,
    val myListStatus: BaseMyListStatus? = null,
    override val isLoading: Boolean = false,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)

    val isNewEntry
        get() = myListStatus == null
}
