package com.axiel7.moelist.ui.composables.media

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.axiel7.moelist.data.model.media.ListStatus

val StatusWatching = Color(0xFF2DB039)
val StatusPlanned = Color(0xFFC3C3C3)
val StatusCompleted = Color(0xFF26448f)
val StatusOnHold = Color(0xFFf1c83e)
val StatusDropped = Color(0xFFa12f31)

@Composable
fun getStatusColor(status: ListStatus?): Color {
    return when (status) {
        ListStatus.WATCHING, ListStatus.READING -> StatusWatching
        ListStatus.PLAN_TO_WATCH,ListStatus.PLAN_TO_READ -> StatusPlanned
        ListStatus.COMPLETED -> StatusCompleted
        ListStatus.ON_HOLD -> StatusOnHold
        ListStatus.DROPPED -> StatusDropped
        else -> MaterialTheme.colorScheme.primaryContainer // A sensible default
    }
}