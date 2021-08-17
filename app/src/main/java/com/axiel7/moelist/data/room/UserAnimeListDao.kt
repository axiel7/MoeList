package com.axiel7.moelist.data.room

import androidx.room.*
import com.axiel7.moelist.data.model.anime.UserAnimeList

@Dao
interface UserAnimeListDao {

    @Query("SELECT * FROM useranimelist WHERE status LIKE :status")
    fun getUserAnimeList(status: String?): List<UserAnimeList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anime: UserAnimeList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<UserAnimeList>)

    @Query("DELETE FROM useranimelist")
    suspend fun clearAll()

    @Delete
    suspend fun deleteUserAnimeList(anime: UserAnimeList): Int

    @Delete
    suspend fun deleteUserAnimeList(animes: List<UserAnimeList>): Int
}