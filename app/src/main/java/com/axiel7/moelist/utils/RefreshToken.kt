package com.axiel7.moelist.utils

import android.util.Log
import com.axiel7.moelist.model.AccessToken
import com.axiel7.moelist.private.ClientId
import com.axiel7.moelist.rest.RefreshTokenService
import com.axiel7.moelist.rest.ServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RefreshToken {

    fun getNewToken(refreshToken: String) :AccessToken? {
        val service = ServiceGenerator.createService(RefreshTokenService::class.java)
        val call : Call<AccessToken> = service.refreshAccessToken(ClientId.clientId, "refresh_token", refreshToken)
        var accessToken: AccessToken?
        accessToken = null
        call.enqueue(object :Callback<AccessToken> {

            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                accessToken = response.body()
            }

            override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                Log.d("MoeLog", "Failed to refresh token")
            }

        })
        return accessToken
    }
}