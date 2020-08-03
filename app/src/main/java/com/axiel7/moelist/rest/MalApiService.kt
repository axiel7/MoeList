package com.axiel7.moelist.rest

import com.axiel7.moelist.model.AnimeListResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface MalApiService {

    @GET("/v2/anime")
    fun getAnimeList(@Query("q") search: String, @Query("limit") limit: Int,
                     @Query("offset") offset: Int, @Query("fields") fields: String): Call<AnimeListResponse>

    @GET
    fun getAnimeDetails(@Url url: String, @Query("fields") fields: String)

}