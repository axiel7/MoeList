package com.axiel7.moelist.data.network

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.model.ApiParams
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.manga.MangaRanking
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.utils.Constants.MAL_API_URL
import com.axiel7.moelist.utils.Constants.MAL_OAUTH2_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters

class Api(private val client: HttpClient) {

    // Login

    suspend fun getAccessToken(
        clientId: String,
        code: String,
        codeVerifier: String,
        grantType: String
    ): AccessToken = client.post("${MAL_OAUTH2_URL}token") {
        setBody(FormDataContent(Parameters.build {
            append("client_id", clientId)
            append("code", code)
            append("code_verifier", codeVerifier)
            append("grant_type", grantType)
        }))
    }.body()

    suspend fun getAccessToken(
        clientId: String,
        refreshToken: String
    ): AccessToken = client.post("${MAL_OAUTH2_URL}token") {
        setBody(FormDataContent(Parameters.build {
            append("client_id", clientId)
            append("refresh_token", refreshToken)
            append("grant_type", "refresh_token")
        }))
    }.body()

    // Anime

    suspend fun getAnimeList(
        params: ApiParams
    ): Response<List<AnimeList>> = client.get("${MAL_API_URL}anime") {
        parameter("q", params.q)
        parameter("limit", params.limit)
        parameter("offset", params.offset)
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
    }.body()

    suspend fun getAnimeList(url: String): Response<List<AnimeList>> = client.get(url).body()

    suspend fun getSeasonalAnime(
        params: ApiParams,
        year: Int,
        season: String
    ): Response<List<AnimeSeasonal>> = client.get("${MAL_API_URL}anime/season/$year/$season") {
        parameter("sort", params.sort)
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
        parameter("limit", params.limit)
    }.body()

    suspend fun getSeasonalAnime(url: String): Response<List<AnimeSeasonal>> =
        client.get(url).body()

    suspend fun getAnimeRanking(
        params: ApiParams,
        rankingType: String
    ): Response<List<AnimeRanking>> = client.get("${MAL_API_URL}anime/ranking") {
        parameter("ranking_type", rankingType)
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
        parameter("limit", params.limit)
    }.body()

    suspend fun getAnimeRanking(url: String): Response<List<AnimeRanking>> = client.get(url).body()

    suspend fun getAnimeRecommendations(
        params: ApiParams = ApiParams()
    ): Response<List<AnimeList>> = client.get("${MAL_API_URL}anime/suggestions") {
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
        parameter("limit", params.limit)
    }.body()

    suspend fun getAnimeRecommendations(url: String): Response<List<AnimeList>> =
        client.get(url).body()

    suspend fun getUserAnimeList(
        params: ApiParams
    ): Response<List<UserAnimeList>> = client.get("${MAL_API_URL}users/@me/animelist") {
        parameter("status", params.status)
        parameter("sort", params.sort)
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
    }.body()

    suspend fun getUserAnimeList(url: String): Response<List<UserAnimeList>> =
        client.get(url).body()

    suspend fun updateUserAnimeList(
        animeId: Int,
        status: String?,
        @IntRange(0, 10) score: Int?,
        watchedEpisodes: Int?,
        startDate: String?,
        endDate: String?,
        isRewatching: Boolean?,
        numRewatches: Int?,
        @IntRange(0, 5) rewatchValue: Int?,
        @IntRange(0, 2) priority: Int?,
        tags: String?,
        comments: String?,
    ): MyAnimeListStatus = client.request("${MAL_API_URL}anime/$animeId/my_list_status") {
        method = HttpMethod.Patch
        setBody(FormDataContent(Parameters.build {
            status?.let { append("status", it) }
            score?.let { append("score", it.toString()) }
            watchedEpisodes?.let { append("num_watched_episodes", it.toString()) }
            startDate?.let { append("start_date", it) }
            endDate?.let { append("finish_date", it) }
            isRewatching?.let { append("is_rewatching", isRewatching.toString()) }
            numRewatches?.let { append("num_times_rewatched", it.toString()) }
            rewatchValue?.let { append("rewatch_value", it.toString()) }
            priority?.let { append("priority", it.toString()) }
            tags?.let { append("tags", it) }
            comments?.let { append("comments", it) }
        }))
    }.body()

    suspend fun deleteAnimeEntry(
        animeId: Int
    ): HttpResponse = client.delete("${MAL_API_URL}anime/$animeId/my_list_status")

    suspend fun getAnimeDetails(
        animeId: Int,
        fields: String?
    ): AnimeDetails = client.get("${MAL_API_URL}anime/$animeId") {
        parameter("fields", fields)
    }.body()

    // Manga

    suspend fun getMangaList(
        params: ApiParams
    ): Response<List<MangaList>> = client.get("${MAL_API_URL}manga") {
        parameter("q", params.q)
        parameter("limit", params.limit)
        parameter("offset", params.offset)
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
    }.body()

    suspend fun getMangaList(url: String): Response<List<MangaList>> = client.get(url).body()

    suspend fun getUserMangaList(
        params: ApiParams
    ): Response<List<UserMangaList>> = client.get("${MAL_API_URL}users/@me/mangalist") {
        parameter("status", params.status)
        parameter("sort", params.sort)
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
    }.body()

    suspend fun getMangaRanking(
        params: ApiParams,
        rankingType: String
    ): Response<List<MangaRanking>> = client.get("${MAL_API_URL}manga/ranking") {
        parameter("ranking_type", rankingType)
        parameter("nsfw", params.nsfw)
        parameter("fields", params.fields)
        parameter("limit", params.limit)
    }.body()

    suspend fun getMangaRanking(url: String): Response<List<MangaRanking>> = client.get(url).body()

    suspend fun getUserMangaList(url: String): Response<List<UserMangaList>> =
        client.get(url).body()

    suspend fun updateUserMangaList(
        mangaId: Int,
        status: String?,
        @IntRange(0, 10) score: Int?,
        chaptersRead: Int?,
        volumesRead: Int?,
        startDate: String?,
        endDate: String?,
        isRereading: Boolean?,
        numRereads: Int?,
        @IntRange(0, 5) rereadValue: Int?,
        @IntRange(0, 2) priority: Int?,
        tags: String?,
        comments: String?,
    ): MyMangaListStatus = client.request("${MAL_API_URL}manga/$mangaId/my_list_status") {
        method = HttpMethod.Patch
        setBody(FormDataContent(Parameters.build {
            status?.let { append("status", it) }
            score?.let { append("score", it.toString()) }
            chaptersRead?.let { append("num_chapters_read", it.toString()) }
            volumesRead?.let { append("num_volumes_read", it.toString()) }
            startDate?.let { append("start_date", it) }
            endDate?.let { append("finish_date", it) }
            isRereading?.let { append("is_rereading", it.toString()) }
            numRereads?.let { append("num_times_reread", it.toString()) }
            rereadValue?.let { append("reread_value", it.toString()) }
            priority?.let { append("priority", it.toString()) }
            tags?.let { append("tags", it) }
            comments?.let { append("comments", it) }
        }))
    }.body()

    suspend fun deleteMangaEntry(
        mangaId: Int
    ): HttpResponse = client.delete("${MAL_API_URL}manga/$mangaId/my_list_status")

    suspend fun getMangaDetails(
        mangaId: Int,
        fields: String?
    ): MangaDetails = client.get("${MAL_API_URL}manga/$mangaId") {
        parameter("fields", fields)
    }.body()

    // User

    suspend fun getUser(
        fields: String?
    ): User = client.get("${MAL_API_URL}users/@me") {
        parameter("fields", fields)
    }.body()
}