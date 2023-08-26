package com.axiel7.moelist

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.axiel7.moelist.data.datastore.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.uicompose.base.ListStyle
import io.ktor.client.HttpClient

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        dataStore = defaultPreferencesDataStore
        createKtorClient(null)
    }

    companion object {

        fun createKtorClient(accessToken: String?) {
            this.accessToken = accessToken
            ktorClient = KtorClient(accessToken).ktorHttpClient
            api = Api(ktorClient)
            jikanApi = JikanApi(ktorClient)
        }

        var accessToken: String? = null
        private lateinit var ktorClient: HttpClient
        lateinit var api: Api
        lateinit var jikanApi: JikanApi
        var dataStore: DataStore<Preferences>? = null
        var nsfw = 0
        var animeListSort = MediaSort.ANIME_TITLE
        var mangaListSort = MediaSort.MANGA_TITLE

        var generalListStyle = ListStyle.STANDARD
        var useGeneralListStyle = true
        var gridItemsPerRow = 0
        var animeCurrentListStyle = ListStyle.STANDARD
        var animePlannedListStyle = ListStyle.STANDARD
        var animeCompletedListStyle = ListStyle.STANDARD
        var animePausedListStyle = ListStyle.STANDARD
        var animeDroppedListStyle = ListStyle.STANDARD
        var mangaCurrentListStyle = ListStyle.STANDARD
        var mangaPlannedListStyle = ListStyle.STANDARD
        var mangaCompletedListStyle = ListStyle.STANDARD
        var mangaPausedListStyle = ListStyle.STANDARD
        var mangaDroppedListStyle = ListStyle.STANDARD

        var titleLanguage = TitleLanguage.ROMAJI
        var useListTabs = false
        var loadCharacters = false
    }
}