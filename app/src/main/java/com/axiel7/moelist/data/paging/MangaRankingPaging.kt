package com.axiel7.moelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.manga.MangaRanking
import com.axiel7.moelist.data.network.Api

class MangaRankingPaging(
    private val api: Api,
    private val apiParams: ApiParams,
    private val rankingType: String
) : PagingSource<String, MangaRanking>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, MangaRanking> {
        return try {
            val nextPage = params.key
            val response = if (nextPage == null) {
                api.getMangaRanking(apiParams, rankingType)
            } else {
                api.getMangaRanking(nextPage)
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

    override fun getRefreshKey(state: PagingState<String, MangaRanking>): String? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)

            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}