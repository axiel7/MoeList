package com.axiel7.moelist.data.repository

import com.axiel7.moelist.App
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.data.network.Api
import kotlinx.coroutines.flow.first

abstract class BaseRepository(
    private val api: Api,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) {
    suspend fun handleResponseError(error: String) {
        when (error) {
            "invalid_token" -> {
                val refreshToken = defaultPreferencesRepository.refreshToken.first()
                if (refreshToken != null) {
                    try {
                        val newToken = api.getAccessToken(
                            clientId = BuildConfig.CLIENT_ID,
                            refreshToken = refreshToken
                        )
                        defaultPreferencesRepository.saveTokens(newToken)
                        App.accessToken = newToken.accessToken
                    } catch (e: Exception) {
                        defaultPreferencesRepository.removeTokens()
                    }
                } else {
                    defaultPreferencesRepository.removeTokens()
                }
            }
        }
    }
}