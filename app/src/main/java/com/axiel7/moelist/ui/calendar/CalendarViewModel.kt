package com.axiel7.moelist.ui.calendar

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val animeRepository: AnimeRepository
) : BaseViewModel<CalendarUiState>(), CalendarEvent {

    override val mutableUiState = MutableStateFlow(CalendarUiState(isLoading = true))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val result = animeRepository.getWeeklyAnime()

            if (result.wasError) {
                showMessage(result.message)
            } else if (result.data != null) {
                mutableUiState.update {
                    it.copy(
                        mondayAnime = result.data[0],
                        tuesdayAnime = result.data[1],
                        wednesdayAnime = result.data[2],
                        thursdayAnime = result.data[3],
                        fridayAnime = result.data[4],
                        saturdayAnime = result.data[5],
                        sundayAnime = result.data[6]
                    )
                }
            }
            setLoading(false)
        }
    }
}