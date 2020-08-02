package com.axiel7.moelist.rest

import com.axiel7.moelist.model.AnimeList
import retrofit2.Call
import retrofit2.http.*

interface MalApiService {

    @GET("/anime")
    fun getAnimeList(@Query("q") search: String, @Query("limit") limit: Int,
                     @Query("offset") offset: Int, @Query("fields") fields: String): Call<AnimeList>

    @GET
    fun getAnimeDetails(@Url url: String, @Query("fields") fields: String)

}