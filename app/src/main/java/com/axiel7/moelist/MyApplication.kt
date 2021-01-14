package com.axiel7.moelist

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.room.AnimeDatabase
import com.axiel7.moelist.utils.CreateOkHttpClient
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import com.google.android.gms.ads.MobileAds
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        SharedPrefsHelpers.init(applicationContext)
        loadSavedPrefs()
        if (isUserLogged) {
            createRetrofit(applicationContext)
        }

        animeDb = AnimeDatabase.getAnimeDatabase(applicationContext)
        MobileAds.initialize(applicationContext)
    }
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .crossfade(true)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(applicationContext))
                    .build()
            }
            .build()
    }

    companion object {
        fun createRetrofit(context: Context) {
            retrofit = Retrofit.Builder()
                .baseUrl(Urls.apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(CreateOkHttpClient.createOkHttpClient(context, false))
                .build()
            malApiService = retrofit.create(MalApiService::class.java)
        }

        fun loadSavedPrefs() {
            val sharedPref = SharedPrefsHelpers.instance!!
            isUserLogged = sharedPref.getBoolean("isUserLogged", false)
            accessToken = sharedPref.getString("accessToken", "null") ?: "null"
            refreshToken = sharedPref.getString("refreshToken", "null") ?: "null"
        }

        var animeDb: AnimeDatabase? = null
        var isUserLogged: Boolean = false
        private lateinit var retrofit: Retrofit
        lateinit var malApiService: MalApiService
        lateinit var accessToken: String
        lateinit var refreshToken: String
    }
}