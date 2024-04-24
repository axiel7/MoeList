package com.axiel7.moelist.ui.calendar

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.ui.base.state.UiState

@Immutable
data class CalendarUiState(
    val mondayAnime: List<AnimeRanking> = emptyList(),
    val tuesdayAnime: List<AnimeRanking> = emptyList(),
    val wednesdayAnime: List<AnimeRanking> = emptyList(),
    val thursdayAnime: List<AnimeRanking> = emptyList(),
    val fridayAnime: List<AnimeRanking> = emptyList(),
    val saturdayAnime: List<AnimeRanking> = emptyList(),
    val sundayAnime: List<AnimeRanking> = emptyList(),
    override val isLoading: Boolean = false,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)

    fun weekAnime(weekday: Int) = when (weekday) {
        1 -> mondayAnime
        2 -> tuesdayAnime
        3 -> wednesdayAnime
        4 -> thursdayAnime
        5 -> fridayAnime
        6 -> saturdayAnime
        7 -> sundayAnime
        else -> emptyList()
    }
}
