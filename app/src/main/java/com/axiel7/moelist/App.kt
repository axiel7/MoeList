package com.axiel7.moelist

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.axiel7.moelist.data.datastore.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.uicompose.base.ListMode
import io.ktor.client.HttpClient
import java.text.NumberFormat

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        dataStore = defaultPreferencesDataStore
    }

    companion object {

        fun createKtorClient(accessToken: String) {
            ktorClient = KtorClient(accessToken).ktorHttpClient
            api = Api(ktorClient)
            jikanApi = JikanApi(ktorClient)
        }

        private lateinit var ktorClient: HttpClient
        lateinit var api: Api
        lateinit var jikanApi: JikanApi
        val numberFormat: NumberFormat = NumberFormat.getInstance()
        var dataStore: DataStore<Preferences>? = null
        var nsfw = 0
        var animeListSort = MediaSort.ANIME_TITLE
        var mangaListSort = MediaSort.MANGA_TITLE
        var listDisplayMode = ListMode.STANDARD
    }
}