package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun RoundedTabRowIndicator(currentTabPosition: TabPosition) {
    TabRowDefaults.Indicator(
        modifier = Modifier
            .tabIndicatorOffset(currentTabPosition)
            .clip(RoundedCornerShape(topStartPercent = 100, topEndPercent = 100)),
        height = 3.dp,
    )
}