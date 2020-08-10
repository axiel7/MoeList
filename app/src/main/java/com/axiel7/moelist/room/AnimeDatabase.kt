package com.axiel7.moelist.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axiel7.moelist.model.AnimeDetails
import com.axiel7.moelist.model.AnimeList
import com.axiel7.moelist.model.AnimeRanking
import com.axiel7.moelist.model.UserAnimeList

@TypeConverters(value = [com.axiel7.moelist.room.TypeConverters::class])
@Database(entities = [AnimeRanking::class, AnimeList::class, AnimeDetails::class, UserAnimeList::class]
    , version = 10)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun rankingAnimeDao(): RankingAnimeDao
    abstract fun listAnimeDao(): ListAnimeDao
    abstract fun animeDetailsDao(): AnimeDetailsDao
    abstract fun userAnimeListDao(): UserAnimeListDao

    companion object {
        @Volatile
        private var INSTANCE: AnimeDatabase? = null

        fun getAnimeDatabase(context: Context): AnimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDatabase::class.java,
                    "animeDatabase"
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}