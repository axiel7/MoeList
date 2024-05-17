package com.axiel7.moelist.di

import android.content.Context
import androidx.room.Room
import com.axiel7.moelist.data.local.MoeListDatabase
import com.axiel7.moelist.data.local.searchhistory.SearchHistoryDao
import com.axiel7.moelist.data.repository.SearchHistoryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    single<MoeListDatabase> { provideDatabase(androidContext()) }
    single<SearchHistoryDao> { get<MoeListDatabase>().searchHistoryDao() }

    singleOf(::SearchHistoryRepository)
}

private fun provideDatabase(context: Context): MoeListDatabase {
    return Room
        .databaseBuilder(
            context = context,
            klass = MoeListDatabase::class.java,
            name = "moelist-database",
        )
        .build()
}
