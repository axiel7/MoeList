package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.AnimeList

@Dao
interface ListAnimeDao {

    @Query("SELECT * FROM anime_list")
    fun getListAnimes(): MutableList<AnimeList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllListAnimes(anime_list: MutableList<AnimeList>)

    @Delete
    fun deleteListAnime(anime_list: AnimeList)

    @Delete
    fun deleteAllListAnimes(anime_list: MutableList<AnimeList>)
}