package com.axiel7.moelist.uicompose.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.App
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.REFRESH_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.private.ClientId
import com.axiel7.moelist.uicompose.base.BaseViewModel
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.PkceGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel: BaseViewModel() {

    private val api = Api(KtorClient(null).ktorHttpClient)
    private val codeVerifier = PkceGenerator.generateVerifier(length = 128)
    val loginUrl = "${Constants.MAL_OAUTH2_URL}authorize?response_type=code&client_id=${ClientId.CLIENT_ID}&code_challenge=${codeVerifier}&state=${LoginViewModel.STATE}"

    var accessToken by mutableStateOf<AccessToken?>(null)
    var loginWasOk by mutableStateOf(false)

    fun getAccessToken(code: String) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            accessToken = try {
                api.getAccessToken(
                    clientId = ClientId.CLIENT_ID,
                    code = code,
                    codeVerifier = codeVerifier,
                    grantType = GRANT_TYPE
                )
            } catch (e: Exception) {
                null
            }

            if (accessToken?.accessToken == null)
                setErrorMessage("Token was null: ${accessToken?.error}: ${accessToken?.message}")
            else {
                App.dataStore?.edit {
                    it[ACCESS_TOKEN_PREFERENCE_KEY] = accessToken!!.accessToken!!
                    it[REFRESH_TOKEN_PREFERENCE_KEY] = accessToken!!.refreshToken!!
                }
                App.createKtorClient(accessToken = accessToken!!.accessToken!!)
                loginWasOk = true
            }

            isLoading = false
        }
    }

    companion object {
        const val STATE = "MoeList123"
        private const val GRANT_TYPE = "authorization_code"
    }
}