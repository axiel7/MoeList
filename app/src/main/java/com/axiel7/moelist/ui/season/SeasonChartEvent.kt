package com.axiel7.moelist.ui.season

import androidx.compose.runtime.Stable
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.SeasonType
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.ui.base.event.PagedUiEvent

@Stable
interface SeasonChartEvent : PagedUiEvent {
    fun setSeason(season: Season? = null, year: Int? = null)
    fun setSeason(type: SeasonType)
    fun onChangeSort(value: MediaSort)
    fun onChangeIsNew(value: Boolean)
    fun onApplyFilters()
}