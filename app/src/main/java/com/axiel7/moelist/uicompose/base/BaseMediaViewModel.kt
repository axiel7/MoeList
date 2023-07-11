package com.axiel7.moelist.uicompose.base

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.MediaType

abstract class BaseMediaViewModel : BaseViewModel() {
    abstract val mediaType: MediaType
    open var mediaInfo: BaseMediaNode? = null
    open var myListStatus: BaseMyListStatus? = null
}