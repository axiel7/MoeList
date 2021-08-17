package com.axiel7.moelist.data.room

import androidx.room.TypeConverter
import com.axiel7.moelist.data.model.*
import com.axiel7.moelist.data.model.anime.*
import com.axiel7.moelist.data.model.manga.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("unused")
class TypeConverters {
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    // node
    @TypeConverter
    fun stringToNode(data: String?): AnimeNode? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun nodeSeasonalToString(someObject: NodeSeasonal?): String {
        return json.encodeToString(someObject)
    }
    @TypeConverter
    fun stringToNodeSeasonal(data: String?): NodeSeasonal? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun nodeToString(someObject: AnimeNode?): String? {
        return json.encodeToString(someObject)
    }

    // seasonal list
    @TypeConverter
    fun stringToSeasonalList(data: String?): MutableList<AnimeSeasonal?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun seasonalListToString(someObject: MutableList<AnimeSeasonal?>?): String? {
        return json.encodeToString(someObject)
    }

    // ranking
    @TypeConverter
    fun stringToRanking(data: String?): Ranking? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun rankingToString(someObject: Ranking?): String? {
        return json.encodeToString(someObject)
    }

    // main picture
    @TypeConverter
    fun stringToMainPicture(data: String?): MainPicture? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun mainPictureToString(someObject: MainPicture?): String? {
        return json.encodeToString(someObject)
    }
    // list main picture
    @TypeConverter
    fun stringToListMainPicture(data: String?): List<MainPicture?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listMainPictureToString(someObject: List<MainPicture?>?): String? {
        return json.encodeToString(someObject)
    }

    // start season
    @TypeConverter
    fun stringToStartSeason(data: String?): StartSeason? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun startSeasonToString(someObject: StartSeason?): String? {
        return json.encodeToString(someObject)
    }

    // alternative titles
    @TypeConverter
    fun stringToAlternativeTitles(data: String?): AlternativeTitles? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun alternativeTitlesToString(someObject: AlternativeTitles?): String? {
        return json.encodeToString(someObject)
    }

    // list genres
    @TypeConverter
    fun stringToListGenres(data: String?): List<Genre?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listGenresToString(someObject: List<Genre?>?): String? {
        return json.encodeToString(someObject)
    }

    // my list status
    @TypeConverter
    fun stringToMyListStatus(data: String?): MyAnimeListStatus? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun myListStatusToString(someObject: MyAnimeListStatus?): String? {
        return json.encodeToString(someObject)
    }

    // broadcast
    @TypeConverter
    fun stringToBroadcast(data: String?): Broadcast? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun broadcastToString(someObject: Broadcast?): String? {
        return json.encodeToString(someObject)
    }

    // list related anime
    @TypeConverter
    fun stringToListRelatedAnime(data: String?): List<Related?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listRelatedAnimeToString(someObject: List<Related?>?): String? {
        return json.encodeToString(someObject)
    }

    // list recommendations
    @TypeConverter
    fun stringToListRecommendations(data: String?): List<Recommendations?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listRecommendationsToString(someObject: List<Recommendations?>?): String? {
        return json.encodeToString(someObject)
    }

    // list studios
    @TypeConverter
    fun stringToListStudios(data: String?): List<Studio?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listStudiosToString(someObject: List<Studio?>?): String? {
        return json.encodeToString(someObject)
    }

    // list themes
    @TypeConverter
    fun stringToListThemes(data: String?): List<Theme?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listThemesToString(someObject: List<Theme?>?): String? {
        return json.encodeToString(someObject)
    }

    // statistics
    @TypeConverter
    fun stringToStatistics(data: String?): Statistics? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun statisticsToString(someObject: Statistics?): String? {
        return json.encodeToString(someObject)
    }
    // statistics status
    @TypeConverter
    fun stringToStatisticsStatus(data: String?): StatisticsStatus? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun statisticsStatusToString(someObject: StatisticsStatus?): String? {
        return json.encodeToString(someObject)
    }


    // manga

    // node
    @TypeConverter
    fun stringToNodeManga(data: String?): MangaNode? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun nodeMangaToString(someObject: MangaNode?): String? {
        return json.encodeToString(someObject)
    }
    // serialization node
    @TypeConverter
    fun stringToSerialNode(data: String?): SerialNode? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun serialNodeToString(someObject: SerialNode?): String? {
        return json.encodeToString(someObject)
    }
    // list serialization node
    @TypeConverter
    fun stringToListSerialNode(data: String?): List<SerialNode?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listSerialNodeToString(someObject: List<SerialNode?>?): String? {
        return json.encodeToString(someObject)
    }
    // serialization
    @TypeConverter
    fun stringToSerialization(data: String?): Serialization? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun serializationToString(someObject: Serialization?): String? {
        return json.encodeToString(someObject)
    }
    // list serialization
    @TypeConverter
    fun stringToListSerial(data: String?): List<Serialization?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listSerialToString(someObject: List<Serialization?>?): String? {
        return json.encodeToString(someObject)
    }

    // author node
    @TypeConverter
    fun stringToAuthorNode(data: String?): AuthorNode? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun authorNodeToString(someObject: AuthorNode?): String? {
        return json.encodeToString(someObject)
    }

    // author
    @TypeConverter
    fun stringToAuthor(data: String?): Author? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun authorToString(someObject: Author?): String? {
        return json.encodeToString(someObject)
    }
    // list author
    @TypeConverter
    fun stringToListAuthor(data: String?): List<Author?>? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun listAuthorToString(someObject: List<Author?>?): String? {
        return json.encodeToString(someObject)
    }

    // my manga list status
    @TypeConverter
    fun stringToMyMangaListStatus(data: String?): MyMangaListStatus? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun myMangaListStatusToString(someObject: MyMangaListStatus?): String? {
        return json.encodeToString(someObject)
    }


    // user

    // user anime statistics
    // my list status
    @TypeConverter
    fun stringToUserAnimeStats(data: String?): UserAnimeStatistics? {
        return if (data != null) json.decodeFromString(data)
        else null
    }
    @TypeConverter
    fun userAnimeStatsToString(someObject: UserAnimeStatistics?): String? {
        return json.encodeToString(someObject)
    }
}