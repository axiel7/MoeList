package com.axiel7.moelist.utils

import android.content.Context
import com.axiel7.moelist.rest.AuthenticationInterceptor
import com.axiel7.moelist.rest.CacheControlInterceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor

object CreateOkHttpClient {
    fun createOkHttpClient(accessToken: String, context: Context, isLogging: Boolean): OkHttpClient {
        val authInterceptor =
            AuthenticationInterceptor("Bearer $accessToken")
        val cacheInterceptor = CacheControlInterceptor(context)
        val logging = HttpLoggingInterceptor()
        if (isLogging) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE)
        }

        return OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .cache(cache = context.let { GetCacheFile.getCacheFile(it, 20) })
            .addInterceptor(authInterceptor)
            .addInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .build()
    }
}