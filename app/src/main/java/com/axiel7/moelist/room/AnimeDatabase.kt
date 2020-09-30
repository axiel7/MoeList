package com.axiel7.moelist.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axiel7.moelist.model.*

@TypeConverters(value = [com.axiel7.moelist.room.TypeConverters::class])
@Database(entities = [SeasonalAnimeResponse::class ,AnimeRanking::class, AnimeList::class,
    SeasonalList::class, AnimeDetails::class, MangaRanking::class, MangaDetails::class,
    UserAnimeList::class, UserMangaList::class, User::class],
    version = 41)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun rankingAnimeDao(): RankingAnimeDao
    abstract fun listAnimeDao(): ListAnimeDao
    abstract fun seasonalResponseDao(): SeasonalResponseDao
    abstract fun seasonalListDao(): SeasonalListDao
    abstract fun animeDetailsDao(): AnimeDetailsDao
    abstract fun rankingMangaDao(): RankingMangaDao
    abstract fun mangaDetailsDao(): MangaDetailsDao
    abstract fun userAnimeListDao(): UserAnimeListDao
    abstract fun userMangaListDao(): UserMangaListDao
    abstract fun userDao(): UserDao

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