package com.axiel7.moelist.utils

import com.axiel7.moelist.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@Suppress("unused")
object ResponseConverter {
    private val gson = Gson()

    // anime list
    fun stringToAnimeListResponse(data: String?): AnimeListResponse? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<AnimeListResponse?>() {}.type
        return gson.fromJson<AnimeListResponse>(data, type)
    }
    fun animeListResponseToString(someObject: AnimeListResponse?): String? {
        return gson.toJson(someObject)
    }
    // anime ranking
    fun stringToAnimeRankResponse(data: String?): AnimeRankingResponse? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<AnimeRankingResponse?>() {}.type
        return gson.fromJson<AnimeRankingResponse>(data, type)
    }
    fun animeRankResponseToString(someObject: AnimeRankingResponse?): String? {
        return gson.toJson(someObject)
    }

    // manga list
    fun stringToMangaListResponse(data: String?): MangaListResponse? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<MangaListResponse?>() {}.type
        return gson.fromJson<MangaListResponse>(data, type)
    }
    fun mangaListResponseToString(someObject: MangaListResponse?): String? {
        return gson.toJson(someObject)
    }
    // manga ranking
    fun stringToMangaRankResponse(data: String?): MangaRankingResponse? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<MangaRankingResponse?>() {}.type
        return gson.fromJson<MangaRankingResponse>(data, type)
    }
    fun mangaRankResponseToString(someObject: MangaRankingResponse?): String? {
        return gson.toJson(someObject)
    }

    // user anime list
    fun stringToUserAnimeListResponse(data: String?): UserAnimeListResponse? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<UserAnimeListResponse?>() {}.type
        return gson.fromJson<UserAnimeListResponse>(data, type)
    }
    fun userAnimeListResponseToString(someObject: UserAnimeListResponse?): String? {
        return gson.toJson(someObject)
    }

    // user manga list
    fun stringToUserMangaListResponse(data: String?): UserMangaListResponse? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<UserMangaListResponse?>() {}.type
        return gson.fromJson<UserMangaListResponse>(data, type)
    }
    fun userMangaListResponseToString(someObject: UserMangaListResponse?): String? {
        return gson.toJson(someObject)
    }

    // user
    fun stringToUserResponse(data: String?): User? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<User?>() {}.type
        return gson.fromJson<User>(data, type)
    }
    fun userResponseToString(someObject: User?): String? {
        return gson.toJson(someObject)
    }
}