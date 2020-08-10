package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.AnimeDetails
import com.axiel7.moelist.model.AnimeList

@Dao
interface AnimeDetailsDao {

    @Query("SELECT * FROM anime_details WHERE id LIKE :animeId ")
    fun getAnimeDetailsById(animeId: Int): AnimeDetails

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnimeDetails(anime_details: AnimeDetails)

    @Delete
    fun deleteAnimeDetails(anime_list: AnimeList)

    @Delete
    fun deleteAllAnimesDetails(anime_list: MutableList<AnimeList>)
}