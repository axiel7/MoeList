package com.axiel7.moelist.ui.season

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.SeasonType
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.ui.base.state.PagedUiState
import com.axiel7.moelist.utils.SeasonCalendar

@Stable
data class SeasonChartUiState(
    val season: StartSeason = SeasonCalendar.currentStartSeason(),
    val sort: MediaSort = MediaSort.ANIME_NUM_USERS,
    val isNew: Boolean = true,
    val seasonType: SeasonType? = SeasonType.CURRENT,
    val animes: SnapshotStateList<AnimeSeasonal> = mutableStateListOf(),
    override val nextPage: String? = null,
    override val loadMore: Boolean = true,
    override val isLoading: Boolean = true,
    override val message: String? = null
) : PagedUiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)

    companion object {
        private const val BASE_YEAR = 1950
        val years = ((SeasonCalendar.currentYear + 1) downTo BASE_YEAR).toList()
    }
}
