package com.axiel7.moelist.room

import androidx.room.TypeConverter
import com.axiel7.moelist.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

@Suppress("unused")
class TypeConverters {
    private val gson = Gson()

    // node
    @TypeConverter
    fun stringToNode(data: String?): Node? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<Node?>() {}.type
        return gson.fromJson<Node>(data, type)
    }
    @TypeConverter
    fun nodeSeasonalToString(someObject: NodeSeasonal?): String? {
        return gson.toJson(someObject)
    }
    @TypeConverter
    fun stringToNodeSeasonal(data: String?): NodeSeasonal? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<NodeSeasonal?>() {}.type
        return gson.fromJson<NodeSeasonal>(data, type)
    }
    @TypeConverter
    fun nodeToString(someObject: Node?): String? {
        return gson.toJson(someObject)
    }

    // seasonal list
    @TypeConverter
    fun stringToSeasonalList(data: String?): MutableList<SeasonalList?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<MutableList<SeasonalList?>?>() {}.type
        return gson.fromJson<MutableList<SeasonalList?>>(data, listType)
    }
    @TypeConverter
    fun seasonalListToString(someObject: MutableList<SeasonalList?>?): String? {
        return gson.toJson(someObject)
    }

    // ranking
    @TypeConverter
    fun stringToRanking(data: String?): Ranking? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<Ranking?>() {}.type
        return gson.fromJson<Ranking>(data, type)
    }
    @TypeConverter
    fun rankingToString(someObject: Ranking?): String? {
        return gson.toJson(someObject)
    }

    // main picture
    @TypeConverter
    fun stringToMainPicture(data: String?): MainPicture? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<MainPicture?>() {}.type
        return gson.fromJson<MainPicture>(data, type)
    }
    @TypeConverter
    fun mainPictureToString(someObject: MainPicture?): String? {
        return gson.toJson(someObject)
    }
    // list main picture
    @TypeConverter
    fun stringToListMainPicture(data: String?): List<MainPicture?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<MainPicture?>?>() {}.type
        return gson.fromJson<List<MainPicture?>>(data, listType)
    }
    @TypeConverter
    fun listMainPictureToString(someObject: List<MainPicture?>?): String? {
        return gson.toJson(someObject)
    }

    // start season
    @TypeConverter
    fun stringToStartSeason(data: String?): StartSeason? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<StartSeason?>() {}.type
        return gson.fromJson<StartSeason>(data, type)
    }
    @TypeConverter
    fun startSeasonToString(someObject: StartSeason?): String? {
        return gson.toJson(someObject)
    }

    // alternative titles
    @TypeConverter
    fun stringToAlternativeTitles(data: String?): AlternativeTitles? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<AlternativeTitles?>() {}.type
        return gson.fromJson<AlternativeTitles>(data, type)
    }
    @TypeConverter
    fun alternativeTitlesToString(someObject: AlternativeTitles?): String? {
        return gson.toJson(someObject)
    }

    // list genres
    @TypeConverter
    fun stringToListGenres(data: String?): List<Genre?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Genre?>?>() {}.type
        return gson.fromJson<List<Genre?>>(data, listType)
    }
    @TypeConverter
    fun listGenresToString(someObject: List<Genre?>?): String? {
        return gson.toJson(someObject)
    }

    // my list status
    @TypeConverter
    fun stringToMyListStatus(data: String?): MyListStatus? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<MyListStatus?>() {}.type
        return gson.fromJson<MyListStatus>(data, type)
    }
    @TypeConverter
    fun myListStatusToString(someObject: MyListStatus?): String? {
        return gson.toJson(someObject)
    }

    // broadcast
    @TypeConverter
    fun stringToBroadcast(data: String?): Broadcast? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<Broadcast?>() {}.type
        return gson.fromJson<Broadcast>(data, type)
    }
    @TypeConverter
    fun broadcastToString(someObject: Broadcast?): String? {
        return gson.toJson(someObject)
    }

    // list related anime
    @TypeConverter
    fun stringToListRelatedAnime(data: String?): List<Related?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Related?>?>() {}.type
        return gson.fromJson<List<Related?>>(data, listType)
    }
    @TypeConverter
    fun listRelatedAnimeToString(someObject: List<Related?>?): String? {
        return gson.toJson(someObject)
    }

    // list recommendations
    @TypeConverter
    fun stringToListRecommendations(data: String?): List<Recommendations?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Recommendations?>?>() {}.type
        return gson.fromJson<List<Recommendations?>>(data, listType)
    }
    @TypeConverter
    fun listRecommendationsToString(someObject: List<Recommendations?>?): String? {
        return gson.toJson(someObject)
    }

    // list studios
    @TypeConverter
    fun stringToListStudios(data: String?): List<Studio?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Studio?>?>() {}.type
        return gson.fromJson<List<Studio?>>(data, listType)
    }
    @TypeConverter
    fun listStudiosToString(someObject: List<Studio?>?): String? {
        return gson.toJson(someObject)
    }

    // list themes
    @TypeConverter
    fun stringToListThemes(data: String?): List<Theme?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Theme?>?>() {}.type
        return gson.fromJson<List<Theme?>>(data, listType)
    }
    @TypeConverter
    fun listThemesToString(someObject: List<Theme?>?): String? {
        return gson.toJson(someObject)
    }

    // statistics
    @TypeConverter
    fun stringToStatistics(data: String?): Statistics? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<Statistics?>() {}.type
        return gson.fromJson<Statistics>(data, type)
    }
    @TypeConverter
    fun statisticsToString(someObject: Statistics?): String? {
        return gson.toJson(someObject)
    }
    // statistics status
    @TypeConverter
    fun stringToStatisticsStatus(data: String?): StatisticsStatus? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<StatisticsStatus?>() {}.type
        return gson.fromJson<StatisticsStatus>(data, type)
    }
    @TypeConverter
    fun statisticsStatusToString(someObject: StatisticsStatus?): String? {
        return gson.toJson(someObject)
    }


    // manga

    // node
    @TypeConverter
    fun stringToNodeManga(data: String?): NodeManga? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<NodeManga?>() {}.type
        return gson.fromJson<NodeManga>(data, type)
    }
    @TypeConverter
    fun nodeMangaToString(someObject: NodeManga?): String? {
        return gson.toJson(someObject)
    }
    // serialization node
    @TypeConverter
    fun stringToSerialNode(data: String?): SerialNode? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<SerialNode?>() {}.type
        return gson.fromJson<SerialNode>(data, type)
    }
    @TypeConverter
    fun serialNodeToString(someObject: SerialNode?): String? {
        return gson.toJson(someObject)
    }
    // list serialization node
    @TypeConverter
    fun stringToListSerialNode(data: String?): List<SerialNode?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<SerialNode?>?>() {}.type
        return gson.fromJson<List<SerialNode?>>(data, listType)
    }
    @TypeConverter
    fun listSerialNodeToString(someObject: List<SerialNode?>?): String? {
        return gson.toJson(someObject)
    }
    // serialization
    @TypeConverter
    fun stringToSerialization(data: String?): Serialization? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<Serialization?>() {}.type
        return gson.fromJson<Serialization>(data, type)
    }
    @TypeConverter
    fun serializationToString(someObject: Serialization?): String? {
        return gson.toJson(someObject)
    }
    // list serialization
    @TypeConverter
    fun stringToListSerial(data: String?): List<Serialization?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Serialization?>?>() {}.type
        return gson.fromJson<List<Serialization?>>(data, listType)
    }
    @TypeConverter
    fun listSerialToString(someObject: List<Serialization?>?): String? {
        return gson.toJson(someObject)
    }

    // author node
    @TypeConverter
    fun stringToAuthorNode(data: String?): AuthorNode? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<AuthorNode?>() {}.type
        return gson.fromJson<AuthorNode>(data, type)
    }
    @TypeConverter
    fun authorNodeToString(someObject: AuthorNode?): String? {
        return gson.toJson(someObject)
    }

    // author
    @TypeConverter
    fun stringToAuthor(data: String?): Author? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<Author?>() {}.type
        return gson.fromJson<Author>(data, type)
    }
    @TypeConverter
    fun authorToString(someObject: Author?): String? {
        return gson.toJson(someObject)
    }
    // list author
    @TypeConverter
    fun stringToListAuthor(data: String?): List<Author?>? {
        if (data==null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Author?>?>() {}.type
        return gson.fromJson<List<Author?>>(data, listType)
    }
    @TypeConverter
    fun listAuthorToString(someObject: List<Author?>?): String? {
        return gson.toJson(someObject)
    }

    // my manga list status
    @TypeConverter
    fun stringToMyMangaListStatus(data: String?): MyMangaListStatus? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<MyMangaListStatus?>() {}.type
        return gson.fromJson<MyMangaListStatus>(data, type)
    }
    @TypeConverter
    fun myMangaListStatusToString(someObject: MyMangaListStatus?): String? {
        return gson.toJson(someObject)
    }


    // user

    // user anime statistics
    // my list status
    @TypeConverter
    fun stringToUserAnimeStats(data: String?): UserAnimeStatistics? {
        if (data==null) {
            return null
        }
        val type: Type = object : TypeToken<UserAnimeStatistics?>() {}.type
        return gson.fromJson<UserAnimeStatistics>(data, type)
    }
    @TypeConverter
    fun userAnimeStatsToString(someObject: UserAnimeStatistics?): String? {
        return gson.toJson(someObject)
    }
}