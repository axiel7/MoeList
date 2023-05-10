package com.axiel7.moelist.data.repository

import androidx.datastore.preferences.core.edit
import com.axiel7.moelist.App
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY

object BaseRepository {
    suspend fun handleResponseError(error: String) {
        when (error) {
            "invalid_token" -> {
                App.dataStore?.edit {
                    it.remove(ACCESS_TOKEN_PREFERENCE_KEY)
                }
            }
        }
    }
}