package com.axiel7.moelist.data.repository

import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeSeasonal

object AnimeRepository {

    //TODAY
    const val FIELDS_TODAY = "broadcast,mean,start_season,status"

    suspend fun getSeasonalAnimes(
        apiParams: ApiParams,
        year: Int,
        season: String,
        page: String? = null
    ): Response<List<AnimeSeasonal>>? {
        return try {
            if (page == null) App.api.getSeasonalAnime(
                params = apiParams,
                year = year,
                season = season
            )
            else App.api.getSeasonalAnime(page)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRecommendedAnimes(
        apiParams: ApiParams,
        page: String? = null
    ): Response<List<AnimeList>>? {
        return try {
            if (page == null) App.api.getAnimeRecommendations(apiParams)
            else App.api.getAnimeRecommendations(page)
        } catch (e: Exception) {
            null
        }
    }
}