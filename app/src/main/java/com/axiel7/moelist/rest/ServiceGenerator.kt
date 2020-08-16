package com.axiel7.moelist.rest

import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceGenerator {
    private const val API_BASE_URL = "https://myanimelist.net"
    private val httpClient = OkHttpClient.Builder()
    private val builder = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())

    fun <S> createService(serviceClass: Class<S>?): S {
        return createService(serviceClass, null)
    }

    fun <S> createService(
        serviceClass: Class<S>?, clientId: String, clientSecret: String?
    ): S {
        if (clientId.isNotEmpty() && clientSecret!!.isNotEmpty()) {
            val authToken: String = Credentials.basic(clientId, clientSecret)
            return createService(serviceClass, authToken)
        }
        return createService(serviceClass, clientId, null)
    }

    private fun <S> createService(
        serviceClass: Class<S>?, authToken: String?
    ): S {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        if (authToken != null) {
            val authInterceptor = AuthenticationInterceptor(authToken)
            if (!httpClient.interceptors().contains(authInterceptor)) {
                httpClient.addInterceptor(authInterceptor)
            }
        }
        httpClient.addInterceptor(logging)
        httpClient.protocols(listOf(Protocol.HTTP_1_1))
        builder.client(httpClient.build())
        val retrofit: Retrofit = builder.build()
        return retrofit.create(serviceClass!!)
    }
}