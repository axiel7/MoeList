package com.axiel7.moelist.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.UserMangaList

@TypeConverters(value = [com.axiel7.moelist.data.room.TypeConverters::class])
@Database(entities = [AnimeDetails::class, MangaDetails::class,
    UserAnimeList::class, UserMangaList::class, User::class],
    version = 48)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun animeDetailsDao(): AnimeDetailsDao
    abstract fun mangaDetailsDao(): MangaDetailsDao
    //abstract fun userAnimeListDao(): UserAnimeListDao
    //abstract fun userMangaListDao(): UserMangaListDao
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