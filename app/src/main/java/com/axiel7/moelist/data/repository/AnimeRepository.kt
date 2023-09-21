package com.axiel7.moelist.data.repository

import androidx.annotation.IntRange
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.CommonApiParams
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.media.Character
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaStatus
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
        sort: MediaSort,
        year: Int,
        season: Season,
        commonApiParams: CommonApiParams,
        page: String? = null,
    ): Response<List<AnimeSeasonal>>? {
        return try {
            val result = if (page == null) App.api.getSeasonalAnime(
                sort = sort,
                params = commonApiParams,
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
        commonApiParams: CommonApiParams,
        page: String? = null
    ): Response<List<AnimeList>>? {
        return try {
            val result = if (page == null) App.api.getAnimeRecommendations(commonApiParams)
            else App.api.getAnimeRecommendations(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    const val LIST_STATUS_FIELDS =
        "start_date,finish_date,num_times_rewatched,is_rewatching,rewatch_value,priority,tags,comments"

    const val ANIME_DETAILS_FIELDS =
        "id,title,main_picture,pictures,alternative_titles,start_date,end_date," +
                "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
                "my_list_status{$LIST_STATUS_FIELDS},num_episodes,start_season,broadcast,source," +
                "average_episode_duration,studios,opening_themes,ending_themes,related_anime{media_type}," +
                "related_manga{media_type},recommendations,background"

    suspend fun getAnimeDetails(
        animeId: Int
    ): AnimeDetails? {
        return try {
            App.api.getAnimeDetails(animeId, ANIME_DETAILS_FIELDS)
        } catch (e: Exception) {
            null
        }
    }

    const val USER_ANIME_LIST_FIELDS =
        "alternative_titles{en,ja},list_status{$LIST_STATUS_FIELDS},num_episodes,media_type,status,broadcast"

    suspend fun getUserAnimeList(
        status: ListStatus,
        sort: MediaSort,
        commonApiParams: CommonApiParams,
        page: String? = null,
    ): Response<List<UserAnimeList>>? {
        return try {
            val result = if (page == null) App.api.getUserAnimeList(
                status = status,
                sort = sort,
                params = commonApiParams
            )
            else App.api.getUserAnimeList(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun updateAnimeEntry(
        animeId: Int,
        status: ListStatus? = null,
        @IntRange(0, 10) score: Int? = null,
        watchedEpisodes: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        isRewatching: Boolean? = null,
        numRewatches: Int? = null,
        @IntRange(0, 5) rewatchValue: Int? = null,
        @IntRange(0, 2) priority: Int? = null,
        tags: String? = null,
        comments: String? = null,
    ): MyAnimeListStatus? {
        return try {
            val result = App.api.updateUserAnimeList(
                animeId,
                status,
                score,
                watchedEpisodes,
                startDate,
                endDate,
                isRewatching,
                numRewatches,
                rewatchValue,
                priority,
                tags,
                comments
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
        query: String,
        commonApiParams: CommonApiParams,
        page: String? = null,
    ): Response<List<AnimeList>>? {
        return try {
            val result = if (page == null) App.api.getAnimeList(
                query = query,
                params = commonApiParams
            )
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
        commonApiParams: CommonApiParams,
        page: String? = null
    ): Response<List<AnimeRanking>>? {
        return try {
            val result =
                if (page == null) App.api.getAnimeRanking(
                    rankingType = rankingType.serialName,
                    params = commonApiParams,
                )
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

    const val CHARACTERS_FIELDS = "id,first_name,last_name,alternative_name,main_picture"

    suspend fun getAnimeCharacters(
        animeId: Int,
        commonApiParams: CommonApiParams,
        page: String? = null
    ): Response<List<Character>>? {
        return try {
            val result = if (page == null) App.api.getAnimeCharacters(animeId, commonApiParams)
            else App.api.getAnimeCharacters(page)
            result.error?.let { BaseRepository.handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
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
                status = ListStatus.WATCHING,
                sort = MediaSort.ANIME_START_DATE,
                params = CommonApiParams(
                    nsfw = nsfw.toInt(),
                    fields = "status,broadcast",
                )
            )

            return result.data?.map { it.node }
                ?.filter { it.broadcast != null && it.status == MediaStatus.AIRING }
                ?.sortedBy { it.broadcast!!.secondsUntilNextBroadcast() }
        } catch (e: Exception) {
            null
        }
    }
}