package com.axiel7.moelist

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.utils.PreferencesDataStore.defaultPreferencesDataStore
import io.ktor.client.HttpClient
import java.text.NumberFormat

class App : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        dataStore = defaultPreferencesDataStore
    }
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .crossfade(true)
            .crossfade(500)
            .error(R.drawable.ic_launcher_foreground)
            .build()
    }

    companion object {

        fun createKtorClient(accessToken: String) {
            ktorClient = KtorClient(accessToken).ktorHttpClient
            api = Api(ktorClient)
        }

        private lateinit var ktorClient: HttpClient
        lateinit var api: Api
        val numberFormat: NumberFormat = NumberFormat.getInstance()
        var dataStore: DataStore<Preferences>? = null
        var nsfw = 0
    }
}