package com.axiel7.moelist.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object PreferencesDataStore {

    val ACCESS_TOKEN_PREFERENCE_KEY = stringPreferencesKey("access_token")
    val NSFW_PREFERENCE_KEY = intPreferencesKey("nsfw")
    val LANG_PREFERENCE_KEY = stringPreferencesKey("lang")
    val THEME_PREFERENCE_KEY = stringPreferencesKey("theme")

    val Context.defaultPreferencesDataStore by preferencesDataStore(name = "default")

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