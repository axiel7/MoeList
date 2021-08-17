package com.axiel7.moelist.data.room

import androidx.room.*
import com.axiel7.moelist.data.model.manga.MangaDetails

@Dao
interface MangaDetailsDao {

    @Query("SELECT * FROM mangadetails WHERE id LIKE :mangaId ")
    fun getMangaDetailsById(mangaId: Int): MangaDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMangaDetails(manga_details: MangaDetails)

    @Delete
    fun deleteMangaDetails(manga_details: MangaDetails)

    @Delete
    fun deleteAllMangaDetails(manga_details: MutableList<MangaDetails>)
}