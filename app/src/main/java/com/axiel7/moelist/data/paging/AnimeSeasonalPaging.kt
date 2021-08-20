package com.axiel7.moelist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.network.Api

class AnimeSeasonalPaging(
    private val api: Api,
    private val apiParams: ApiParams,
    private val startSeason: StartSeason
) : PagingSource<String, AnimeSeasonal>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, AnimeSeasonal> {
        return try {
            val nextPage = params.key
            val response = if (nextPage == null) {
                api.getSeasonalAnime(apiParams, startSeason.year, startSeason.season)
            } else {
                api.getSeasonalAnime(nextPage)
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

    override fun getRefreshKey(state: PagingState<String, AnimeSeasonal>): String? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)

            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}