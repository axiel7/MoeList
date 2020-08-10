package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.UserAnimeList

@Dao
interface UserAnimeListDao {

    @Query("SELECT * FROM user_anime_list WHERE status LIKE :status")
    fun getUserAnimeListByStatus(status: String): MutableList<UserAnimeList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserAnimeList(user_anime_list: MutableList<UserAnimeList>)

    @Delete
    fun deleteUserAnimeListEntry(user_anime_list: UserAnimeList)

    @Delete
    fun deleteUserAnimeList(user_anime_list: MutableList<UserAnimeList>)
}