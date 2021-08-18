package com.axiel7.moelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.network.Api
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class UserMangaListPaging(
    private val api: Api,
    private val apiParams: ApiParams
) : PagingSource<String, UserMangaList>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, UserMangaList> {
        return try {
            val nextPage = params.key
            val response = if (nextPage==null) {
                api.getUserMangaList(apiParams)
            } else {
                api.getUserMangaList(nextPage)
            }
            LoadResult.Page(
                data = response.data!!,
                prevKey = null,
                nextKey = response.paging?.next
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, UserMangaList>): String? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)

            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}