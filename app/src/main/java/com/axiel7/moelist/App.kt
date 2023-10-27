package com.axiel7.moelist

import android.app.Application
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.di.dataStoreModule
import com.axiel7.moelist.di.networkModule
import com.axiel7.moelist.di.repositoryModule
import com.axiel7.moelist.di.viewModelModule
import com.axiel7.moelist.di.workerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

class App : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.IS_DEBUG) {
                androidLogger()
            }
            androidContext(this@App)
            workManagerFactory()
            modules(networkModule, dataStoreModule, repositoryModule, viewModelModule, workerModule)
        }
    }

    companion object {
        var accessToken: String? = null
        var titleLanguage = TitleLanguage.ROMAJI
    }
}