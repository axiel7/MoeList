package com.axiel7.moelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.network.Api
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class UserAnimeListPaging(
    private val api: Api,
    private val apiParams: ApiParams
) : PagingSource<String, UserAnimeList>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, UserAnimeList> {
        return try {
            val nextPage = params.key
            val response = if (nextPage==null) {
                api.getUserAnimeList(apiParams)
            } else {
                api.getUserAnimeList(nextPage)
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

    override fun getRefreshKey(state: PagingState<String, UserAnimeList>): String? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)

            val prevUrl = anchorPage?.prevKey?.toHttpUrlOrNull()
            val prevOffset = prevUrl?.queryParameter("offset")?.toIntOrNull() ?: 0
            //Log.d("moepage", "prevOffset $prevOffset")
            val prevLimit = prevUrl?.queryParameter("limit")?.toIntOrNull() ?: 100
            //Log.d("moepage", "prevLimit $prevLimit")
            val prevUrlNew = prevUrl?.newBuilder()
                ?.removeAllQueryParameters("offset")
                ?.addQueryParameter("offset", (prevOffset ).toString())
                ?.build()

            val nextUrl = anchorPage?.nextKey?.toHttpUrlOrNull()
            val nextOffset = nextUrl?.queryParameter("offset")?.toIntOrNull() ?: 100
            //Log.d("moepage", "nextOffset $nextOffset")
            //val nextLimit = nextUrl?.queryParameter("limit")?.toIntOrNull() ?: 100
            //Log.d("moepage", "nextLimit $nextLimit")
            val nextUrlNew = nextUrl?.newBuilder()
                ?.removeAllQueryParameters("offset")
                ?.addQueryParameter("offset", (nextOffset * 2).toString())
                ?.build()

            prevUrlNew?.toString() ?: nextUrlNew?.toString()
        }
    }
}