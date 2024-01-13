package com.axiel7.moelist.ui.search

import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.event.PagedUiEvent

interface SearchEvent : PagedUiEvent {
    fun search(query: String)
    fun onChangeMediaType(value: MediaType)
}