package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.App
import com.axiel7.moelist.data.datastore.PreferencesDataStore

data class ListType(
    val status: ListStatus,
    val mediaType: MediaType,
) {
    val stylePreferenceKey get() = when (status) {
        ListStatus.WATCHING -> PreferencesDataStore.ANIME_CURRENT_LIST_STYLE_PREFERENCE_KEY
        ListStatus.READING -> PreferencesDataStore.MANGA_CURRENT_LIST_STYLE_PREFERENCE_KEY
        ListStatus.PTW -> PreferencesDataStore.ANIME_PLANNED_LIST_STYLE_PREFERENCE_KEY
        ListStatus.PTR -> PreferencesDataStore.MANGA_PLANNED_LIST_STYLE_PREFERENCE_KEY
        ListStatus.COMPLETED ->
            if (mediaType == MediaType.ANIME) PreferencesDataStore.ANIME_COMPLETED_LIST_STYLE_PREFERENCE_KEY
            else PreferencesDataStore.MANGA_COMPLETED_LIST_STYLE_PREFERENCE_KEY
        ListStatus.ON_HOLD ->
            if (mediaType == MediaType.ANIME) PreferencesDataStore.ANIME_PAUSED_LIST_STYLE_PREFERENCE_KEY
            else PreferencesDataStore.MANGA_PAUSED_LIST_STYLE_PREFERENCE_KEY
        ListStatus.DROPPED ->
            if (mediaType == MediaType.ANIME) PreferencesDataStore.ANIME_DROPPED_LIST_STYLE_PREFERENCE_KEY
            else PreferencesDataStore.MANGA_DROPPED_LIST_STYLE_PREFERENCE_KEY
    }

    val styleGlobalAppVariable get() = when (status) {
        ListStatus.WATCHING -> App.animeCurrentListStyle
        ListStatus.READING -> App.mangaCurrentListStyle
        ListStatus.PTW -> App.animePlannedListStyle
        ListStatus.PTR -> App.mangaPlannedListStyle
        ListStatus.COMPLETED ->
            if (mediaType == MediaType.ANIME) App.animeCompletedListStyle
            else App.mangaCompletedListStyle
        ListStatus.ON_HOLD ->
            if (mediaType == MediaType.ANIME) App.animePausedListStyle
            else App.mangaPausedListStyle
        ListStatus.DROPPED ->
            if (mediaType == MediaType.ANIME) App.animeDroppedListStyle
            else App.mangaDroppedListStyle
    }
}
