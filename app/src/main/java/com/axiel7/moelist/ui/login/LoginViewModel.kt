package com.axiel7.moelist.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.private.ClientId
import com.axiel7.moelist.utils.Constants.MAL_OAUTH2_URL
import com.axiel7.moelist.utils.PkceGenerator
import io.ktor.client.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val ktorClient: HttpClient by lazy {
        KtorClient(null).ktorHttpClient
    }
    private val api: Api by lazy { Api(ktorClient) }

    private val codeVerifier = PkceGenerator.generateVerifier(length = 128)
    val loginUrl = "${MAL_OAUTH2_URL}authorize?response_type=code&client_id=${ClientId.CLIENT_ID}&code_challenge=${codeVerifier}&state=$STATE"

    private val _accessToken = MutableStateFlow<AccessToken?>(null)
    val accessToken: StateFlow<AccessToken?> = _accessToken

    fun getAccessToken(code: String) {
        viewModelScope.launch {
            val call = async { api.getAccessToken(
                clientId = ClientId.CLIENT_ID,
                code = code,
                codeVerifier = codeVerifier,
                grantType = "authorization_code"
            ) }

            val result = try {
                call.await()
            } catch (e: Exception) {
                null
            }

            _accessToken.value = result
        }
    }

    companion object {
        const val STATE = "MoeList123"
    }
}