package com.axiel7.moelist.utils

import android.content.Context
import com.axiel7.moelist.rest.AuthenticationInterceptor
import com.axiel7.moelist.rest.CacheControl
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor

object CreateOkHttpClient {
    fun createOkHttpClient(context: Context, isLogging: Boolean): OkHttpClient {
        SharedPrefsHelpers.init(context)
        val sharedPrefs = SharedPrefsHelpers.instance
        val accessToken = sharedPrefs?.getString("accessToken", "").toString()
        val authInterceptor =
            AuthenticationInterceptor("Bearer $accessToken")
        val cacheInterceptor = CacheControl(context)
        val logging = HttpLoggingInterceptor()
        if (isLogging) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE)
        }

        return OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .cache(cache = getCacheFile(context))
            .addInterceptor(authInterceptor)
            .addInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .build()
    }
    private fun getCacheFile(context: Context) : Cache? {
        val cacheSize = (50 * 1024 * 1024).toLong() // 50MiB
        return context.cacheDir?.let { Cache(it, cacheSize) }
    }
}