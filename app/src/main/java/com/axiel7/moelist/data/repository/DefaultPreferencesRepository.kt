package com.axiel7.moelist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.di.getValue
import com.axiel7.moelist.di.setValue
import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.utils.NumExtensions.toInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val accessToken = dataStore.getValue(ACCESS_TOKEN_KEY)
    val refreshToken = dataStore.getValue(REFRESH_TOKEN_KEY)

    suspend fun saveTokens(value: AccessToken) {
        dataStore.edit {
            if (value.accessToken != null) it[ACCESS_TOKEN_KEY] = value.accessToken
            if (value.refreshToken != null) it[REFRESH_TOKEN_KEY] = value.refreshToken
        }
    }

    suspend fun removeTokens() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
        }
    }

    suspend fun clearUserData() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
            it.remove(PROFILE_PICTURE_KEY)
            it.remove(USERNAME_KEY)
        }
    }

    suspend fun saveCodeVerifier(value: String) {
        dataStore.setValue(CODE_VERIFIER_KEY, value)
    }

    suspend fun getCodeVerifier(): String? {
        return dataStore.getValue(CODE_VERIFIER_KEY).first()
    }

    suspend fun removeCodeVerifier() {
        dataStore.edit {
            it.remove(CODE_VERIFIER_KEY)
        }
    }

    val nsfw = dataStore.getValue(NSFW_KEY, false)
    suspend fun nsfwInt() = nsfw.first().toInt()
    suspend fun setNsfw(value: Boolean) {
        dataStore.setValue(NSFW_KEY, value)
    }

    val hideScores = dataStore.getValue(HIDE_SCORES_KEY, false)
    suspend fun setHideScores(value: Boolean) {
        dataStore.setValue(HIDE_SCORES_KEY, value)
    }

    val theme = dataStore.getValue(THEME_KEY, ThemeStyle.FOLLOW_SYSTEM.name)
        .map { ThemeStyle.valueOfOrNull(it) ?: ThemeStyle.FOLLOW_SYSTEM }
    suspend fun setTheme(value: ThemeStyle) {
        dataStore.setValue(THEME_KEY, value.name)
    }

    val useBlackColors = dataStore.getValue(USE_BLACK_COLORS_KEY, false)
    suspend fun setUseBlackColors(value: Boolean) {
        dataStore.setValue(USE_BLACK_COLORS_KEY, value)
    }

    val useMonochrome = dataStore.getValue(USE_MONOCHROME_KEY, false)
    suspend fun setUseMonochrome(value: Boolean) {
        dataStore.setValue(USE_MONOCHROME_KEY, value)
    }

    val isOnboardingCompleted = dataStore.getValue(ONBOARDING_COMPLETED_KEY, false)
    suspend fun setOnboardingCompleted(value: Boolean) {
        dataStore.setValue(ONBOARDING_COMPLETED_KEY, value)
    }

    val lastTab = dataStore.getValue(LAST_TAB_KEY, 0)
    suspend fun setLastTab(value: Int) {
        dataStore.setValue(LAST_TAB_KEY, value)
    }

    val pinnedNavBar = dataStore.getValue(PINNED_NAV_BAR_KEY, false)
    suspend fun setPinnedNavBar(value: Boolean) {
        dataStore.setValue(PINNED_NAV_BAR_KEY, value)
    }

    val profilePicture = dataStore.getValue(PROFILE_PICTURE_KEY)
    suspend fun setProfilePicture(value: String) {
        dataStore.setValue(PROFILE_PICTURE_KEY, value)
    }

    val username = dataStore.getValue(USERNAME_KEY)
    suspend fun setUsername(value: String) {
        dataStore.setValue(USERNAME_KEY, value)
    }

    val animeListStatus = dataStore.getValue(ANIME_LIST_STATUS_KEY, ListStatus.WATCHING.name)
        .map { ListStatus.valueOf(it) }

    suspend fun setAnimeListStatus(value: ListStatus) {
        dataStore.setValue(ANIME_LIST_STATUS_KEY, value.name)
    }

    val mangaListStatus = dataStore.getValue(MANGA_LIST_STATUS_KEY, ListStatus.READING.name)
        .map { ListStatus.valueOf(it) }

    suspend fun setMangaListStatus(value: ListStatus) {
        dataStore.setValue(MANGA_LIST_STATUS_KEY, value.name)
    }

    val animeListSort = dataStore.getValue(ANIME_LIST_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeListSort(value: MediaSort) {
        dataStore.setValue(ANIME_LIST_SORT_KEY, value.value)
    }

    val mangaListSort = dataStore.getValue(MANGA_LIST_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaListSort(value: MediaSort) {
        dataStore.setValue(MANGA_LIST_SORT_KEY, value.value)
    }

    val animeWatchingSort = dataStore.getValue(ANIME_WATCHING_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeWatchingSort(value: MediaSort) {
        dataStore.setValue(ANIME_WATCHING_SORT_KEY, value.value)
    }

    val animePlannedSort = dataStore.getValue(ANIME_PLANNED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimePlannedSort(value: MediaSort) {
        dataStore.setValue(ANIME_PLANNED_SORT_KEY, value.value)
    }

    val animeCompletedSort = dataStore.getValue(ANIME_COMPLETED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeCompletedSort(value: MediaSort) {
        dataStore.setValue(ANIME_COMPLETED_SORT_KEY, value.value)
    }

    val animePausedSort = dataStore.getValue(ANIME_PAUSED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimePausedSort(value: MediaSort) {
        dataStore.setValue(ANIME_PAUSED_SORT_KEY, value.value)
    }

    val animeDroppedSort = dataStore.getValue(ANIME_DROPPED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeDroppedSort(value: MediaSort) {
        dataStore.setValue(ANIME_DROPPED_SORT_KEY, value.value)
    }

    val mangaReadingSort = dataStore.getValue(MANGA_READING_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaReadingSort(value: MediaSort) {
        dataStore.setValue(MANGA_READING_SORT_KEY, value.value)
    }

    val mangaPlannedSort = dataStore.getValue(MANGA_PLANNED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaPlannedSort(value: MediaSort) {
        dataStore.setValue(MANGA_PLANNED_SORT_KEY, value.value)
    }

    val mangaCompletedSort = dataStore.getValue(MANGA_COMPLETED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaCompletedSort(value: MediaSort) {
        dataStore.setValue(MANGA_COMPLETED_SORT_KEY, value.value)
    }

    val mangaPausedSort = dataStore.getValue(MANGA_PAUSED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaPausedSort(value: MediaSort) {
        dataStore.setValue(MANGA_PAUSED_SORT_KEY, value.value)
    }

    val mangaDroppedSort = dataStore.getValue(MANGA_DROPPED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaDroppedSort(value: MediaSort) {
        dataStore.setValue(MANGA_DROPPED_SORT_KEY, value.value)
    }

    val startTab = dataStore.getValue(START_TAB_KEY, StartTab.LAST_USED.value)
        .map { StartTab.valueOf(tabName = it) }
    suspend fun setStartTab(value: StartTab) {
        dataStore.setValue(START_TAB_KEY, value.value)
    }

    val tabletMode = dataStore.getValue(TABLET_MODE_KEY, TabletMode.AUTO.name)
        .map { TabletMode.valueOf(it) }
    suspend fun setTabletMode(value: TabletMode) {
        dataStore.setValue(TABLET_MODE_KEY, value.name)
    }

    val titleLang = dataStore.getValue(TITLE_LANG_KEY, TitleLanguage.ROMAJI.name)
        .map { TitleLanguage.valueOf(it) }

    suspend fun setTitleLang(value: TitleLanguage) {
        dataStore.setValue(TITLE_LANG_KEY, value.name)
        App.titleLanguage = value
    }

    val useListTabs = dataStore.getValue(USE_LIST_TABS_KEY, false)
    suspend fun setUseListTabs(value: Boolean) {
        dataStore.setValue(USE_LIST_TABS_KEY, value)
    }

    val loadCharacters = dataStore.getValue(LOAD_CHARACTERS_KEY, false)
    suspend fun setLoadCharacters(value: Boolean) {
        dataStore.setValue(LOAD_CHARACTERS_KEY, value)
    }

    val randomListEntryEnabled = dataStore.getValue(RANDOM_LIST_ENTRY_KEY, false)
    suspend fun setRandomListEntryEnabled(value: Boolean) {
        dataStore.setValue(RANDOM_LIST_ENTRY_KEY, value)
    }

    val generalListStyle = dataStore.getValue(GENERAL_LIST_STYLE_KEY, ListStyle.STANDARD.name)
        .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setGeneralListStyle(value: ListStyle) {
        dataStore.setValue(GENERAL_LIST_STYLE_KEY, value.name)
    }

    val useGeneralListStyle = dataStore.getValue(USE_GENERAL_LIST_STYLE_KEY, true)
    suspend fun setUseGeneralListStyle(value: Boolean) {
        dataStore.setValue(USE_GENERAL_LIST_STYLE_KEY, value)
    }

    val gridItemsPerRow = dataStore.getValue(GRID_ITEMS_PER_ROW_KEY, ItemsPerRow.DEFAULT.value)
        .map { ItemsPerRow.valueOf(it) ?: ItemsPerRow.DEFAULT }

    suspend fun setGridItemsPerRow(value: ItemsPerRow) {
        dataStore.setValue(GRID_ITEMS_PER_ROW_KEY, value.value)
    }

    val animeCurrentListStyle =
        dataStore.getValue(ANIME_CURRENT_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setAnimeCurrentListStyle(value: ListStyle) {
        dataStore.setValue(ANIME_CURRENT_LIST_STYLE_KEY, value.name)
    }

    val animePlannedListStyle =
        dataStore.getValue(ANIME_PLANNED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setAnimePlannedListStyle(value: ListStyle) {
        dataStore.setValue(ANIME_PLANNED_LIST_STYLE_KEY, value.name)
    }

    val animeCompletedListStyle =
        dataStore.getValue(ANIME_COMPLETED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setAnimeCompletedListStyle(value: ListStyle) {
        dataStore.setValue(ANIME_COMPLETED_LIST_STYLE_KEY, value.name)
    }

    val animePausedListStyle =
        dataStore.getValue(ANIME_PAUSED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setAnimePausedListStyle(value: ListStyle) {
        dataStore.setValue(ANIME_PAUSED_LIST_STYLE_KEY, value.name)
    }

    val animeDroppedListStyle =
        dataStore.getValue(ANIME_DROPPED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setAnimeDroppedListStyle(value: ListStyle) {
        dataStore.setValue(ANIME_DROPPED_LIST_STYLE_KEY, value.name)
    }

    val mangaCurrentListStyle =
        dataStore.getValue(MANGA_CURRENT_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setMangaCurrentListStyle(value: ListStyle) {
        dataStore.setValue(MANGA_CURRENT_LIST_STYLE_KEY, value.name)
    }

    val mangaPlannedListStyle =
        dataStore.getValue(MANGA_PLANNED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setMangaPlannedListStyle(value: ListStyle) {
        dataStore.setValue(MANGA_PLANNED_LIST_STYLE_KEY, value.name)
    }

    val mangaCompletedListStyle =
        dataStore.getValue(MANGA_COMPLETED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setMangaCompletedListStyle(value: ListStyle) {
        dataStore.setValue(MANGA_COMPLETED_LIST_STYLE_KEY, value.name)
    }

    val mangaPausedListStyle =
        dataStore.getValue(MANGA_PAUSED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setMangaPausedListStyle(value: ListStyle) {
        dataStore.setValue(MANGA_PAUSED_LIST_STYLE_KEY, value.name)
    }

    val mangaDroppedListStyle =
        dataStore.getValue(MANGA_DROPPED_LIST_STYLE_KEY, ListStyle.STANDARD.name)
            .map { ListStyle.valueOfOrNull(it) ?: ListStyle.STANDARD }

    suspend fun setMangaDroppedListStyle(value: ListStyle) {
        dataStore.setValue(MANGA_DROPPED_LIST_STYLE_KEY, value.name)
    }

    companion object {

        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val CODE_VERIFIER_KEY = stringPreferencesKey("code_verifier")

        private val NSFW_KEY = booleanPreferencesKey("nsfw")
        private val HIDE_SCORES_KEY = booleanPreferencesKey("hide_scores")
        private val THEME_KEY = stringPreferencesKey("theme")
        private val USE_BLACK_COLORS_KEY = booleanPreferencesKey("use_black_colors")
        private val USE_MONOCHROME_KEY = booleanPreferencesKey("use_monochrome")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val LAST_TAB_KEY = intPreferencesKey("last_tab")
        private val PINNED_NAV_BAR_KEY = booleanPreferencesKey("pinned_nav_bar")
        private val PROFILE_PICTURE_KEY = stringPreferencesKey("profile_picture")
        private val USERNAME_KEY = stringPreferencesKey("username")

        private val ANIME_LIST_STATUS_KEY = stringPreferencesKey("anime_list_status")
        private val MANGA_LIST_STATUS_KEY = stringPreferencesKey("manga_list_status")
        private val ANIME_LIST_SORT_KEY = stringPreferencesKey("anime_list_sort")
        private val MANGA_LIST_SORT_KEY = stringPreferencesKey("manga_list_sort")
        private val ANIME_WATCHING_SORT_KEY = stringPreferencesKey("anime_watching_sort")
        private val ANIME_PLANNED_SORT_KEY = stringPreferencesKey("anime_planned_sort")
        private val ANIME_COMPLETED_SORT_KEY = stringPreferencesKey("anime_completed_sort")
        private val ANIME_PAUSED_SORT_KEY = stringPreferencesKey("anime_paused_sort")
        private val ANIME_DROPPED_SORT_KEY = stringPreferencesKey("anime_dropped_sort")
        private val MANGA_READING_SORT_KEY = stringPreferencesKey("manga_reading_sort")
        private val MANGA_PLANNED_SORT_KEY = stringPreferencesKey("manga_planned_sort")
        private val MANGA_COMPLETED_SORT_KEY = stringPreferencesKey("manga_completed_sort")
        private val MANGA_PAUSED_SORT_KEY = stringPreferencesKey("manga_paused_sort")
        private val MANGA_DROPPED_SORT_KEY = stringPreferencesKey("manga_dropped_sort")

        private val START_TAB_KEY = stringPreferencesKey("start_tab")
        private val TABLET_MODE_KEY = stringPreferencesKey("tablet_mode")
        private val TITLE_LANG_KEY = stringPreferencesKey("title_lang")
        private val USE_LIST_TABS_KEY = booleanPreferencesKey("use_list_tabs")
        private val LOAD_CHARACTERS_KEY = booleanPreferencesKey("load_characters")
        private val RANDOM_LIST_ENTRY_KEY = booleanPreferencesKey("random_list_entry_enabled")

        private val GENERAL_LIST_STYLE_KEY = stringPreferencesKey("list_display_mode")
        private val USE_GENERAL_LIST_STYLE_KEY = booleanPreferencesKey("use_general_list_style")
        private val GRID_ITEMS_PER_ROW_KEY = intPreferencesKey("grid_items_per_row")

        private val ANIME_CURRENT_LIST_STYLE_KEY = stringPreferencesKey("anime_current_list_style")
        private val ANIME_PLANNED_LIST_STYLE_KEY = stringPreferencesKey("anime_planned_list_style")
        private val ANIME_COMPLETED_LIST_STYLE_KEY =
            stringPreferencesKey("anime_completed_list_style")
        private val ANIME_PAUSED_LIST_STYLE_KEY = stringPreferencesKey("anime_paused_list_style")
        private val ANIME_DROPPED_LIST_STYLE_KEY = stringPreferencesKey("anime_dropped_list_style")
        private val MANGA_CURRENT_LIST_STYLE_KEY = stringPreferencesKey("manga_current_list_style")
        private val MANGA_PLANNED_LIST_STYLE_KEY = stringPreferencesKey("manga_planned_list_style")
        private val MANGA_COMPLETED_LIST_STYLE_KEY =
            stringPreferencesKey("manga_completed_list_style")
        private val MANGA_PAUSED_LIST_STYLE_KEY = stringPreferencesKey("manga_paused_list_style")
        private val MANGA_DROPPED_LIST_STYLE_KEY = stringPreferencesKey("manga_dropped_list_style")
    }
}