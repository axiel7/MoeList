package com.axiel7.moelist

import android.app.Application
import com.axiel7.moelist.room.AnimeDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        animeDb = AnimeDatabase.getAnimeDatabase(applicationContext)
    }

    companion object {
        var animeDb: AnimeDatabase? = null
    }
}