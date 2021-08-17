package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlternativeTitles(
    @SerialName("synonyms")
    val synonyms: List<String>,
    @SerialName("en")
    val en: String,
    @SerialName("ja")
    val ja: String
)