package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.UserMangaList

@Dao
interface UserMangaListDao {

    @Query("SELECT * FROM user_manga_list WHERE status LIKE :status")
    fun getUserMangaListByStatus(status: String): MutableList<UserMangaList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserMangaList(user_manga_list: MutableList<UserMangaList>)

    @Delete
    fun deleteUserMangaListEntry(user_manga_list: UserMangaList)

    @Delete
    fun deleteUserMangaList(user_manga_list: MutableList<UserMangaList>)
}