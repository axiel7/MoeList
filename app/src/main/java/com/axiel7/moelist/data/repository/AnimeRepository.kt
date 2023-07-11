package com.axiel7.moelist.data.repository

import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.anime.secondsUntilNextBroadcast
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.KtorClient
import com.axiel7.moelist.utils.NumExtensions.toInt
import io.ktor.http.HttpStatusCode

object AnimeRepository {

    const val TODAY_FIELDS = "alternative_titles{en,ja},broadcast,mean,start_season,status"
    const val CALENDAR_FIELDS =
        "alternative_titles{en,ja},broadcast,mean,start_season,status,media_type,num_episodes"
    const val SEASONAL_FIELDS =
        "alternative_titles{en,ja},start_season,broadcast,num_episodes,media_type,mean"

    suspend fun getSeasonalAnimes(
        apiParams: ApiParams,
        year: Int,
        season: Season,
        page: String? = null
    ): Response<List<AnimeSeasonal>>? {
        return try {
            val result = if (page == null) App.api.getSeasonalAnime(
                params = apiParams,
                year = year,
                season = season.value
            )
            else App.api.getSeasonalAnime(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    const val RECOMMENDED_FIELDS = "alternative_titles{en,ja},mean"

    suspend fun getRecommendedAnimes(
        apiParams: ApiParams,
        page: String? = null
    ): Response<List<AnimeList>>? {
        return try {
            val result = if (page == null) App.api.getAnimeRecommendations(apiParams)
            else App.api.getAnimeRecommendations(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    const val ANIME_DETAILS_FIELDS =
        "id,title,main_picture,pictures,alternative_titles,start_date,end_date," +
                "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
                "my_list_status{num_times_rewatched},num_episodes,start_season,broadcast,source," +
                "average_episode_duration,studios,opening_themes,ending_themes,related_anime{media_type}," +
                "related_manga{media_type},recommendations"

    suspend fun getAnimeDetails(
        animeId: Int
    ): AnimeDetails? {
        return try {
            //App.animeDb.animeDetailsDao().getAnimeDetailsById(animeId)?.let { return it }
            App.api.getAnimeDetails(animeId, ANIME_DETAILS_FIELDS)
        } catch (e: Exception) {
            null
        }
    }

    const val USER_ANIME_LIST_FIELDS =
        "alternative_titles{en,ja},list_status{num_times_rewatched},num_episodes,media_type,status,broadcast"

    suspend fun getUserAnimeList(
        apiParams: ApiParams,
        page: String? = null
    ): Response<List<UserAnimeList>>? {
        return try {
            val result = if (page == null) App.api.getUserAnimeList(apiParams)
            else App.api.getUserAnimeList(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun updateAnimeEntry(
        animeId: Int,
        status: String?,
        score: Int?,
        watchedEpisodes: Int?,
        startDate: String?,
        endDate: String?,
        numRewatches: Int?,
    ): MyAnimeListStatus? {
        return try {
            val result = App.api.updateUserAnimeList(
                animeId,
                status,
                score,
                watchedEpisodes,
                startDate,
                endDate,
                numRewatches
            )
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteAnimeEntry(
        animeId: Int
    ): Boolean {
        return try {
            val result = App.api.deleteAnimeEntry(animeId)
            return result.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    const val SEARCH_FIELDS =
        "id,title,alternative_titles{en,ja},main_picture,mean,media_type,num_episodes,num_chapters,start_season"

    suspend fun searchAnime(
        apiParams: ApiParams,
        page: String? = null
    ): Response<List<AnimeList>>? {
        return try {
            val result = if (page == null) App.api.getAnimeList(apiParams)
            else App.api.getAnimeList(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    const val RANKING_FIELDS =
        "alternative_titles{en,ja},mean,media_type,num_episodes,num_chapters,num_list_users"

    suspend fun getAnimeRanking(
        rankingType: RankingType,
        apiParams: ApiParams,
        page: String? = null
    ): Response<List<AnimeRanking>>? {
        return try {
            val result = if (page == null) App.api.getAnimeRanking(apiParams, rankingType.value)
            else App.api.getAnimeRanking(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getAnimeAiringStatus(
        animeId: Int
    ): AnimeDetails? {
        return try {
            App.api.getAnimeDetails(animeId, fields = "id,status")
        } catch (e: Exception) {
            null
        }
    }

    // widget
    suspend fun getAiringAnimeOnList(
        token: String,
        nsfw: Boolean
    ): List<AnimeNode>? {
        return try {
            val api = Api(KtorClient(token).ktorHttpClient)
            val result: Response<List<UserAnimeList>> = api.getUserAnimeList(
                ApiParams(
                    status = ListStatus.WATCHING.value,
                    sort = MediaSort.ANIME_START_DATE.value,
                    nsfw = nsfw.toInt(),
                    fields = "status,broadcast",
                )
            )

            return result.data?.map { it.node }
                ?.filter { it.broadcast != null && it.status == "currently_airing" }
                ?.sortedBy { it.broadcast!!.secondsUntilNextBroadcast() }
        } catch (e: Exception) {
            null
        }
    }
}