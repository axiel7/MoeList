package com.axiel7.moelist.uicompose.more.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.uicompose.base.ListStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListStyleSettingsViewModel(
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : ViewModel() {

    fun getListStyle(mediaType: MediaType, status: ListStatus) =
        when (mediaType) {
            MediaType.ANIME -> {
                when (status) {
                    ListStatus.WATCHING -> defaultPreferencesRepository.animeCurrentListStyle
                    ListStatus.PTW -> defaultPreferencesRepository.animePlannedListStyle
                    ListStatus.COMPLETED -> defaultPreferencesRepository.animeCompletedListStyle
                    ListStatus.ON_HOLD -> defaultPreferencesRepository.animePausedListStyle
                    ListStatus.DROPPED -> defaultPreferencesRepository.animeDroppedListStyle
                    else -> flowOf(null)
                }
            }

            MediaType.MANGA -> {
                when (status) {
                    ListStatus.READING -> defaultPreferencesRepository.mangaCurrentListStyle
                    ListStatus.PTR -> defaultPreferencesRepository.mangaPlannedListStyle
                    ListStatus.COMPLETED -> defaultPreferencesRepository.mangaCompletedListStyle
                    ListStatus.ON_HOLD -> defaultPreferencesRepository.mangaPausedListStyle
                    ListStatus.DROPPED -> defaultPreferencesRepository.mangaDroppedListStyle
                    else -> flowOf(null)
                }
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    fun setListStyle(
        mediaType: MediaType,
        status: ListStatus,
        value: ListStyle
    ) = viewModelScope.launch {
        when (mediaType) {
            MediaType.ANIME -> {
                when (status) {
                    ListStatus.WATCHING ->
                        defaultPreferencesRepository.setAnimeCurrentListStyle(value)

                    ListStatus.PTW ->
                        defaultPreferencesRepository.setAnimePlannedListStyle(value)

                    ListStatus.COMPLETED ->
                        defaultPreferencesRepository.setAnimeCompletedListStyle(value)

                    ListStatus.ON_HOLD ->
                        defaultPreferencesRepository.setAnimePausedListStyle(value)

                    ListStatus.DROPPED ->
                        defaultPreferencesRepository.setAnimeDroppedListStyle(value)

                    else -> {}
                }
            }

            MediaType.MANGA -> {
                when (status) {
                    ListStatus.READING ->
                        defaultPreferencesRepository.setMangaCurrentListStyle(value)

                    ListStatus.PTR ->
                        defaultPreferencesRepository.setMangaPlannedListStyle(value)

                    ListStatus.COMPLETED ->
                        defaultPreferencesRepository.setMangaCompletedListStyle(value)

                    ListStatus.ON_HOLD ->
                        defaultPreferencesRepository.setMangaPausedListStyle(value)

                    ListStatus.DROPPED ->
                        defaultPreferencesRepository.setMangaDroppedListStyle(value)

                    else -> {}
                }
            }
        }
    }
}