package com.axiel7.moelist.ui.editmedia

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.base.event.UiEvent
import java.time.LocalDate

interface EditMediaEvent : UiEvent {

    fun setMediaInfo(value: BaseMediaNode)

    fun setEditVariables(myListStatus: BaseMyListStatus)

    fun onChangeStatus(value: ListStatus)

    fun onChangeProgress(value: Int?)

    fun onChangeVolumeProgress(value: Int?)

    fun onChangeScore(value: Int)

    fun onChangeStartDate(value: LocalDate?)

    fun openStartDatePicker()

    fun onChangeFinishDate(value: LocalDate?)

    fun openFinishDatePicker()

    fun closeDatePickers()

    fun onChangeTags(value: String)

    fun onChangePriority(value: Int)

    fun onChangeIsRepeating(value: Boolean)

    fun onChangeRepeatCount(value: Int?)

    fun onChangeRepeatValue(value: Int)

    fun onChangeComments(value: String)

    fun updateListItem()

    fun toggleDeleteDialog(open: Boolean)

    fun deleteEntry()

    fun onDismiss()
}