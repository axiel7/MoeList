package com.axiel7.moelist.data.local.searchhistory

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("select * from search_history order by updated_at DESC limit 10")
    fun getItems(): Flow<List<SearchHistoryEntity>>

    @Upsert
    suspend fun addItem(item: SearchHistoryEntity)

    @Delete
    suspend fun deleteItem(item: SearchHistoryEntity)
}
