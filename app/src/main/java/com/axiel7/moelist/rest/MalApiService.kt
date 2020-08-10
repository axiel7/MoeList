package com.axiel7.moelist.rest

import com.axiel7.moelist.model.*
import retrofit2.Call
import retrofit2.http.*

interface MalApiService {

    @GET("/v2/anime")
    fun getAnimeList(@Query("q") search: String,
                     @Query("limit") limit: Int?,
                     @Query("offset") offset: Int?,
                     @Query("fields") fields: String?): Call<AnimeListResponse>

    @GET
    fun getSeasonalAnime(@Url url: String, @Query("sort") sort: String): Call<SeasonalAnimeResponse>

    @GET("/v2/anime/ranking")
    fun getAnimeRanking(@Query("ranking_type") rankingType: String,
                        @Query("fields") fields: String): Call<AnimeRankingResponse>

    @GET("/v2/anime/suggestions")
    fun getAnimeRecommend(@Query("limit") limit: Int): Call<AnimeListResponse>

    @GET("/v2/users/@me/animelist")
    fun getUserAnimeList(@Query("status") status: String,
                         @Query("fields") fields: String,
                         @Query("sort") sort: String): Call<UserAnimeListResponse>

    @GET
    fun getNextRankingPage(@Url url: String): Call<AnimeRankingResponse>
    @GET
    fun getNextRecommendPage(@Url url: String): Call<AnimeListResponse>
    @GET
    fun getNextAnimeListPage(@Url url: String): Call<UserAnimeListResponse>

    @GET
    fun getAnimeDetails(@Url url: String, @Query("fields") fields: String): Call<AnimeDetails>


    //TODO (implement: is_rewatching, priotity, num_times_rewatched, rewatch_value, tags, comments)
    @FormUrlEncoded
    @PATCH
    fun updateAnimeList(@Url url: String,
                        @Field("status") status: String?,
                        @Field("score") score: Int?,
                        @Field("num_watched_episodes") watchedEpisodes: Int?): Call<MyListStatus>

    @DELETE
    fun deleteAnimeEntry(@Url url: String): Call<Void>
}