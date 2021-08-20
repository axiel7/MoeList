package com.axiel7.moelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.network.Api

class AnimeRankingPaging(
    private val api: Api,
    private val apiParams: ApiParams,
    private val rankingType: String
) : PagingSource<String, AnimeRanking>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, AnimeRanking> {
        return try {
            val nextPage = params.key
            val response = if (nextPage == null) {
                api.getAnimeRanking(apiParams, rankingType)
            } else {
                api.getAnimeRanking(nextPage)
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

    override fun getRefreshKey(state: PagingState<String, AnimeRanking>): String? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)

            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}