package com.axiel7.moelist.data.repository

import androidx.datastore.preferences.core.edit
import com.axiel7.moelist.App
import com.axiel7.moelist.data.datastore.PreferencesDataStore
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.private.ClientId
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.PkceGenerator

object LoginRepository {

    const val STATE = "MoeList123"
    private const val GRANT_TYPE = "authorization_code"
    private val codeVerifier = PkceGenerator.generateVerifier(length = 128)
    val loginUrl =
        "${Constants.MAL_OAUTH2_URL}authorize?response_type=code&client_id=${ClientId.CLIENT_ID}&code_challenge=${codeVerifier}&state=${STATE}"

    suspend fun getAccessToken(code: String): Response<AccessToken> {
        val accessToken = try {
            App.api.getAccessToken(
                clientId = ClientId.CLIENT_ID,
                code = code,
                codeVerifier = codeVerifier,
                grantType = GRANT_TYPE
            )
        } catch (e: Exception) {
            null
        }

        return if (accessToken?.accessToken == null)
            Response(message = "Token was null: ${accessToken?.error}: ${accessToken?.message}")
        else {
            App.dataStore?.edit {
                it[PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY] = accessToken.accessToken
                it[PreferencesDataStore.REFRESH_TOKEN_PREFERENCE_KEY] = accessToken.refreshToken!!
            }
            App.createKtorClient(accessToken = accessToken.accessToken)
            Response(data = accessToken)
        }
    }

}