package com.axiel7.moelist.rest

import com.axiel7.moelist.model.AccessToken
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RefreshTokenService {
    @FormUrlEncoded
    @POST("/v1/oauth2/token")
    fun refreshAccessToken(@Field("client_id") clientId: String,
                       @Field("grant_type") grantType: String,
                       @Field("refresh_token") refreshToken: String) : Call<AccessToken>
}