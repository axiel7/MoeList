package com.axiel7.moelist.data.room

import androidx.room.*
import com.axiel7.moelist.data.model.anime.AnimeDetails

@Dao
interface AnimeDetailsDao {

    @Query("SELECT * FROM animedetails WHERE id LIKE :animeId ")
    fun getAnimeDetailsById(animeId: Int): AnimeDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnimeDetails(anime_details: AnimeDetails)

    @Delete
    fun deleteAnimeDetails(anime_details: AnimeDetails)

    @Delete
    fun deleteAllAnimesDetails(anime_details: MutableList<AnimeDetails>)
}