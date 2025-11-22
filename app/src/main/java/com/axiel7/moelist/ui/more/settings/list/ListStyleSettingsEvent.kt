package com.axiel7.moelist.ui.more.settings.list

import androidx.compose.runtime.Stable
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.ListStyle
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ListStyleSettingsEvent {
    fun getListStyle(mediaType: MediaType, status: ListStatus): StateFlow<ListStyle?>
    fun setListStyle(mediaType: MediaType, status: ListStatus, value: ListStyle)
}