package com.axiel7.moelist.data.model

import java.time.LocalDateTime

data class SearchHistory(
    val keyword: String,
    val updatedAt: LocalDateTime,
)
