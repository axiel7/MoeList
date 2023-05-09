package com.axiel7.moelist.uicompose.airingwidget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.axiel7.moelist.data.model.anime.AnimeNode
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object AiringInfoStateDefinition : GlanceStateDefinition<AiringInfo> {

    private const val DATA_STORE_FILENAME = "airing_info"

    private val Context.datastore by dataStore(DATA_STORE_FILENAME, AiringInfoSerializer)

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<AiringInfo> {
        return context.datastore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME)
    }

    object AiringInfoSerializer : Serializer<AiringInfo> {
        override val defaultValue = AiringInfo.Unavailable(message = "Empty")

        override suspend fun readFrom(input: InputStream): AiringInfo = try {
            Json.decodeFromString(
                AiringInfo.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Could not read data: ${exception.message}")
        }

        override suspend fun writeTo(t: AiringInfo, output: OutputStream) {
            output.use {
                it.write(Json.encodeToString(AiringInfo.serializer(), t).encodeToByteArray())
            }
        }
    }
}

@Serializable
sealed interface AiringInfo {
    @Serializable
    object Loading : AiringInfo

    @Serializable
    data class Available(
        val animeList: List<AnimeNode>
    ) : AiringInfo

    @Serializable
    data class Unavailable(val message: String) : AiringInfo
}