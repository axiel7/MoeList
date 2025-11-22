package com.axiel7.moelist.data.model

import androidx.compose.runtime.Stable
import java.time.LocalDateTime

@Stable
data class SearchHistory(
    val keyword: String,
    val updatedAt: LocalDateTime,
)
