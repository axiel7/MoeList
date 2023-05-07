package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MainPicture
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaNode(
    @SerialName("id")
    override val id: Int,
    @SerialName("title")
    override val title: String,
    @SerialName("main_picture")
    override val mainPicture: MainPicture? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("num_volumes")
    val numVolumes: Int? = null,
    @SerialName("num_chapters")
    val numChapters: Int? = null,
    @SerialName("num_list_users")
    override val numListUsers: Int? = null,
    @SerialName("media_type")
    override val mediaType: String? = null,
    @SerialName("status")
    override val status: String? = null,
    @SerialName("mean")
    override val mean: Float? = null
): BaseMediaNode()