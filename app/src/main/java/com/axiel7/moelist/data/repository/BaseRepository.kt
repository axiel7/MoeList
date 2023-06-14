package com.axiel7.moelist.data.repository

import androidx.datastore.preferences.core.edit
import com.axiel7.moelist.App
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.REFRESH_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.getValueSync
import com.axiel7.moelist.private.ClientId

object BaseRepository {
    suspend fun handleResponseError(error: String) {
        when (error) {
            "invalid_token" -> {
                val refreshToken = App.dataStore?.getValueSync(REFRESH_TOKEN_PREFERENCE_KEY)
                if (refreshToken != null) {
                    try {
                        val newToken = App.api.getAccessToken(
                            clientId = ClientId.CLIENT_ID,
                            refreshToken = refreshToken
                        )
                        App.dataStore?.edit {
                            it[ACCESS_TOKEN_PREFERENCE_KEY] = newToken.accessToken!!
                            it[REFRESH_TOKEN_PREFERENCE_KEY] = newToken.refreshToken!!
                        }
                        App.createKtorClient(newToken.accessToken!!)
                    } catch (e: Exception) {
                        deleteAccessToken()
                    }
                } else {
                    deleteAccessToken()
                }
            }
        }
    }

    private suspend fun deleteAccessToken() {
        App.dataStore?.edit {
            it.remove(ACCESS_TOKEN_PREFERENCE_KEY)
            it.remove(REFRESH_TOKEN_PREFERENCE_KEY)
        }
    }
}