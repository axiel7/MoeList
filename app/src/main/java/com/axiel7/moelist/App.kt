package com.axiel7.moelist

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.axiel7.moelist.Anilist.AnilistQuery
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.di.dataStoreModule
import com.axiel7.moelist.di.databaseModule
import com.axiel7.moelist.di.networkModule
import com.axiel7.moelist.di.repositoryModule
import com.axiel7.moelist.di.viewModelModule
import com.axiel7.moelist.di.workerModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

class App : Application(), KoinComponent, SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@App)
            workManagerFactory()
            modules(
                networkModule,
                dataStoreModule,
                repositoryModule,
                viewModelModule,
                workerModule,
                databaseModule,
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            AnilistQuery.appThis = this;
            AnilistQuery.cache = AnilistQuery.New_ObjectKache()
        }

    }

    override fun newImageLoader(context: PlatformContext) =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()

    companion object {
        var accessToken: String? = null
        var titleLanguage = TitleLanguage.ROMAJI
    }
}
