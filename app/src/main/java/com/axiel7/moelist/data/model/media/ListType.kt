package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.data.repository.DefaultPreferencesRepository

data class ListType(
    val status: ListStatus,
    val mediaType: MediaType,
) {
    fun stylePreference(
        defaultPreferencesRepository: DefaultPreferencesRepository
    ) = when (status) {
        ListStatus.WATCHING -> defaultPreferencesRepository.animeCurrentListStyle
        ListStatus.READING -> defaultPreferencesRepository.mangaCurrentListStyle
        ListStatus.PTW -> defaultPreferencesRepository.animePlannedListStyle
        ListStatus.PTR -> defaultPreferencesRepository.mangaPlannedListStyle
        ListStatus.COMPLETED ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.animeCompletedListStyle
            else defaultPreferencesRepository.mangaCompletedListStyle

        ListStatus.ON_HOLD ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.animePausedListStyle
            else defaultPreferencesRepository.mangaPausedListStyle

        ListStatus.DROPPED ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.animeDroppedListStyle
            else defaultPreferencesRepository.mangaDroppedListStyle
        }
}
