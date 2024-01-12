package com.axiel7.moelist.ui.base

import androidx.lifecycle.SavedStateHandle
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.details.MEDIA_TYPE_ARGUMENT
import com.axiel7.moelist.utils.StringExtensions.removeFirstAndLast

abstract class BaseMediaViewModel(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    val mediaType: MediaType = MediaType.valueOf(
        savedStateHandle.get<String>(MEDIA_TYPE_ARGUMENT.removeFirstAndLast())!!
    )
    protected open var _mediaInfo: BaseMediaNode? = null
    val mediaInfo get() = _mediaInfo
    open fun setMediaInfo(value: BaseMediaNode?) {
        _mediaInfo = value
    }

    protected open var _myListStatus: BaseMyListStatus? = null
    val myListStatus get() = _myListStatus
    open fun setMyListStatus(value: BaseMyListStatus?) {
        _myListStatus = value
    }
}