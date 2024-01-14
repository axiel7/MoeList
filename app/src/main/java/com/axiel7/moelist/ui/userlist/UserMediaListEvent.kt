package com.axiel7.moelist.ui.userlist

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.ui.base.event.PagedUiEvent

interface UserMediaListEvent : PagedUiEvent {
    fun onChangeStatus(value: ListStatus)
    fun onChangeSort(value: MediaSort)
    fun onUpdateProgress(item: BaseUserMediaList<out BaseMediaNode>)
    fun onItemSelected(item: BaseUserMediaList<*>)
    fun onChangeItemMyListStatus(value: BaseMyListStatus?, removed: Boolean = false)
    fun setAsCompleted(mediaId: Int)
    fun refreshList()
    fun toggleSortDialog(open: Boolean)
    fun toggleSetAsCompleteDialog(open: Boolean)
    fun getRandomIdOfList()
    fun onRandomIdOpen()
}