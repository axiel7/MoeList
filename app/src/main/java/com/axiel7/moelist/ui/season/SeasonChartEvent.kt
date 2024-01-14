package com.axiel7.moelist.ui.season

import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.ui.base.event.PagedUiEvent

interface SeasonChartEvent : PagedUiEvent {
    fun setSeason(season: Season? = null, year: Int? = null)
    fun onChangeSort(value: MediaSort)
    fun onApplyFilters()
}