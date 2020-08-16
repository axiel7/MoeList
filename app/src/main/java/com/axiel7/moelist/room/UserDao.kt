package com.axiel7.moelist.room

import androidx.room.*
import com.axiel7.moelist.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE id LIKE :userId ")
    fun getUserById(userId: Int): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Delete
    fun deleteUser(user: User)

    @Delete
    fun deleteAllUsers(user_list: MutableList<User>)
}