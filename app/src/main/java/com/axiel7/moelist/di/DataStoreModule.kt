package com.axiel7.moelist.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val DEFAULT_DATA_STORE = "default"
const val NOTIFICATIONS_DATA_STORE = "notifications"

val dataStoreModule = module {
    single(named(DEFAULT_DATA_STORE)) { provideDataStore(androidApplication(), DEFAULT_DATA_STORE) }
    single(named(NOTIFICATIONS_DATA_STORE)) { provideDataStore(androidApplication(), NOTIFICATIONS_DATA_STORE) }
}

private fun provideDataStore(context: Context, name: String) =
    PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile(name)
    }

fun <T> DataStore<Preferences>.getValue(key: Preferences.Key<T>) = data.map { it[key] }

fun <T> DataStore<Preferences>.getValue(
    key: Preferences.Key<T>,
    default: T,
) = data.map { it[key] ?: default }

suspend fun <T> DataStore<Preferences>.setValue(
    key: Preferences.Key<T>,
    value: T?
) = edit {
    if (value != null) it[key] = value
    else it.remove(key)
}

fun <T> DataStore<Preferences>.getValueBlocking(key: Preferences.Key<T>) =
    runBlocking { data.first() }[key]