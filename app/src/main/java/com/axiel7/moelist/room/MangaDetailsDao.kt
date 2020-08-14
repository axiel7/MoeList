package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.MangaDetails

@Dao
interface MangaDetailsDao {

    @Query("SELECT * FROM manga_details WHERE id LIKE :mangaId ")
    fun getMangaDetailsById(mangaId: Int): MangaDetails

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMangaDetails(manga_details: MangaDetails)

    @Delete
    fun deleteMangaDetails(manga_details: MangaDetails)

    @Delete
    fun deleteAllMangaDetails(manga_details: MutableList<MangaDetails>)
}