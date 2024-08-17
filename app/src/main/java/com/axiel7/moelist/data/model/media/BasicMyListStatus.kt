package com.axiel7.moelist.data.model.media

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class BasicMyListStatus(
    val status: ListStatus
)
