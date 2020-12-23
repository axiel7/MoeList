package com.axiel7.moelist

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import com.axiel7.moelist.room.AnimeDatabase
import com.google.android.gms.ads.MobileAds
import okhttp3.OkHttpClient

class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
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
        var animeDb: AnimeDatabase? = null
    }
}