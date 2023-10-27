package com.axiel7.moelist.di

import com.axiel7.moelist.worker.NotificationWorker
import com.axiel7.moelist.worker.NotificationWorkerManager
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val workerModule = module {
    single {
        NotificationWorkerManager(
            context = androidApplication(),
            dataStore = get(named(NOTIFICATIONS_DATA_STORE)))
    }
    workerOf(::NotificationWorker)
}