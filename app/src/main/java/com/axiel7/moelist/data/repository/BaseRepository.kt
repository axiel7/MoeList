package com.axiel7.moelist.data.repository

import androidx.datastore.preferences.core.edit
import com.axiel7.moelist.App
import com.axiel7.moelist.utils.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY

object BaseRepository {
    suspend fun handleResponseError(error: String) {
        when (error) {
            "invalid_token" -> {
                App.dataStore?.edit {
                    it[ACCESS_TOKEN_PREFERENCE_KEY] = ""
                }
            }
        }
    }
}