package com.axiel7.moelist.rest

import com.axiel7.moelist.model.AccessToken2
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginService {

    @FormUrlEncoded
    @POST("/v1/oauth2/token")
    fun getAccessToken(@Field("client_id") clientId: String,
                       @Field("code") code: String,
                       @Field("code_verifier") codeVerifier: String,
                       @Field("grant_type") grantType: String) :Call<AccessToken2>
}