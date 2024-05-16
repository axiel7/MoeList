package com.axiel7.moelist.data.repository

import com.axiel7.moelist.data.local.searchhistory.SearchHistoryDao
import com.axiel7.moelist.data.local.searchhistory.SearchHistoryEntity
import com.axiel7.moelist.data.local.searchhistory.toSearchEntity
import com.axiel7.moelist.data.local.searchhistory.toSearchHistoryList
import com.axiel7.moelist.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchHistoryRepository(
    private val dao: SearchHistoryDao,
) {
    fun getItems(): Flow<List<SearchHistory>> {
        return dao.getItems().map(List<SearchHistoryEntity>::toSearchHistoryList)
    }

    suspend fun addItem(query: String) {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isNotBlank()) {
            dao.addItem(SearchHistoryEntity(keyword = trimmedQuery))
        }
    }

    suspend fun deleteItem(item: SearchHistory) {
        dao.deleteItem(item.toSearchEntity())
    }
}
