package com.axiel7.moelist

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.data.room.AnimeDatabase
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.google.firebase.analytics.FirebaseAnalytics
import io.ktor.client.*

class App : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        SharedPrefsHelpers.init(applicationContext)
        if (isUserLogged) {
            createKtorClient()
        }

        animeDb = AnimeDatabase.getAnimeDatabase(applicationContext)

        FirebaseAnalytics.getInstance(applicationContext).apply {
            setAnalyticsCollectionEnabled(sendAnalytics)
        }
    }
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .crossfade(true)
            .crossfade(500)
            .error(R.drawable.ic_launcher_foreground)
            .build()
    }

    companion object {

        fun createKtorClient() {
            ktorClient = KtorClient(accessToken).ktorHttpClient
            api = Api(ktorClient)
        }

        val isUserLogged: Boolean
        get() = SharedPrefsHelpers.instance?.getBoolean("user_logged", false) ?: false

        val accessToken: String
        get() = SharedPrefsHelpers.instance?.getString("access_token", "null") ?: "null"

        val refreshToken: String
        get() = SharedPrefsHelpers.instance?.getString("refresh_token", "null") ?: "null"

        val sendAnalytics: Boolean
        get() = SharedPrefsHelpers.instance?.getBoolean("send_analytics", true) ?: true

        lateinit var animeDb: AnimeDatabase
        private lateinit var ktorClient: HttpClient
        lateinit var api: Api
    }
}