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
        ListStatus.PLAN_TO_WATCH -> defaultPreferencesRepository.animePlannedListStyle
        ListStatus.PLAN_TO_READ -> defaultPreferencesRepository.mangaPlannedListStyle
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

    fun sortPreference(
        defaultPreferencesRepository: DefaultPreferencesRepository
    ) = when (status) {
        ListStatus.WATCHING -> defaultPreferencesRepository.animeWatchingSort
        ListStatus.READING -> defaultPreferencesRepository.mangaReadingSort
        ListStatus.PLAN_TO_WATCH -> defaultPreferencesRepository.animePlannedSort
        ListStatus.PLAN_TO_READ -> defaultPreferencesRepository.mangaPlannedSort
        ListStatus.COMPLETED ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.animeCompletedSort
            else defaultPreferencesRepository.mangaCompletedSort

        ListStatus.ON_HOLD ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.animePausedSort
            else defaultPreferencesRepository.mangaPausedSort

        ListStatus.DROPPED ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.animeDroppedSort
            else defaultPreferencesRepository.mangaDroppedSort
    }

    suspend fun setSortPreference(
        defaultPreferencesRepository: DefaultPreferencesRepository,
        value: MediaSort
    ) = when (status) {
        ListStatus.WATCHING -> defaultPreferencesRepository.setAnimeWatchingSort(value)
        ListStatus.READING -> defaultPreferencesRepository.setMangaReadingSort(value)
        ListStatus.PLAN_TO_WATCH -> defaultPreferencesRepository.setAnimePlannedSort(value)
        ListStatus.PLAN_TO_READ -> defaultPreferencesRepository.setMangaPlannedSort(value)
        ListStatus.COMPLETED ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.setAnimeCompletedSort(value)
            else defaultPreferencesRepository.setMangaCompletedSort(value)

        ListStatus.ON_HOLD ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.setAnimePausedSort(value)
            else defaultPreferencesRepository.setMangaPausedSort(value)

        ListStatus.DROPPED ->
            if (mediaType == MediaType.ANIME) defaultPreferencesRepository.setAnimeDroppedSort(value)
            else defaultPreferencesRepository.setMangaDroppedSort(value)
    }
}
