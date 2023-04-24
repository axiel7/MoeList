package com.axiel7.moelist

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.utils.ContextExtensions.changeTheme
import com.axiel7.moelist.utils.SharedPrefsHelpers
import io.ktor.client.*
import java.text.NumberFormat

class App : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        SharedPrefsHelpers.init(applicationContext)
        changeTheme()
        if (isUserLogged) {
            createKtorClient()
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

        val accessToken: String?
        get() = SharedPrefsHelpers.instance?.getString("access_token", null)

        val refreshToken: String
        get() = SharedPrefsHelpers.instance?.getString("refresh_token", "null") ?: "null"

        val sendAnalytics: Boolean
        get() = SharedPrefsHelpers.instance?.getBoolean("send_analytics", true) ?: true

        val nsfw: Boolean
        get() = SharedPrefsHelpers.instance?.getBoolean("nsfw", false) ?: false

        private lateinit var ktorClient: HttpClient
        lateinit var api: Api
        val numberFormat: NumberFormat = NumberFormat.getInstance()
    }
}