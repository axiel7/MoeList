package com.axiel7.moelist.ui.base.state

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus

interface MediaForEdit {
    val mediaInfo: BaseMediaNode?
    val myListStatus: BaseMyListStatus?
}