package com.axiel7.moelist.data.datastore

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object PreferencesDataStore {

    val ACCESS_TOKEN_PREFERENCE_KEY = stringPreferencesKey("access_token")
    val REFRESH_TOKEN_PREFERENCE_KEY = stringPreferencesKey("refresh_token")
    val NSFW_PREFERENCE_KEY = booleanPreferencesKey("nsfw")
    val LANG_PREFERENCE_KEY = stringPreferencesKey("lang")
    val THEME_PREFERENCE_KEY = stringPreferencesKey("theme")
    val LAST_TAB_PREFERENCE_KEY = intPreferencesKey("last_tab")
    val PROFILE_PICTURE_PREFERENCE_KEY = stringPreferencesKey("profile_picture")
    val ANIME_LIST_SORT_PREFERENCE_KEY = stringPreferencesKey("anime_list_sort")
    val MANGA_LIST_SORT_PREFERENCE_KEY = stringPreferencesKey("manga_list_sort")

    val GENERAL_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("list_display_mode")
    val USE_GENERAL_LIST_STYLE_PREFERENCE_KEY = booleanPreferencesKey("use_general_list_style")
    val ANIME_CURRENT_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("anime_current_list_style")
    val ANIME_PLANNED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("anime_planned_list_style")
    val ANIME_COMPLETED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("anime_completed_list_style")
    val ANIME_PAUSED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("anime_paused_list_style")
    val ANIME_DROPPED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("anime_dropped_list_style")
    val MANGA_CURRENT_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("manga_current_list_style")
    val MANGA_PLANNED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("manga_planned_list_style")
    val MANGA_COMPLETED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("manga_completed_list_style")
    val MANGA_PAUSED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("manga_paused_list_style")
    val MANGA_DROPPED_LIST_STYLE_PREFERENCE_KEY = stringPreferencesKey("manga_dropped_list_style")

    val START_TAB_PREFERENCE_KEY = stringPreferencesKey("start_tab")
    val TITLE_LANG_PREFERENCE_KEY = stringPreferencesKey("title_lang")
    val USE_LIST_TABS_PREFERENCE_KEY = booleanPreferencesKey("use_list_tabs")

    val Context.defaultPreferencesDataStore by preferencesDataStore(name = "default")
    val Context.notificationsDataStore by preferencesDataStore(name = "notifications")

    /**
     * Gets the value by blocking the main thread
     */
    fun <T> DataStore<Preferences>.getValueSync(
        key: Preferences.Key<T>
    ) = runBlocking { data.first() }[key]

    @Composable
    fun <T> rememberPreference(
        key: Preferences.Key<T>,
        defaultValue: T,
    ): MutableState<T> {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val state = remember {
            context.defaultPreferencesDataStore.data
                .map {
                    it[key] ?: defaultValue
                }
        }.collectAsState(initial = defaultValue)

        return remember {
            object : MutableState<T> {
                override var value: T
                    get() = state.value
                    set(value) {
                        coroutineScope.launch {
                            context.defaultPreferencesDataStore.edit {
                                it[key] = value
                            }
                        }
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { value = it }
            }
        }
    }
}