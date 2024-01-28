package com.axiel7.moelist.ui.composables.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.LocalizableAndColorable
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.ui.composables.Rectangle
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.NumExtensions.format

@Composable
fun <T : LocalizableAndColorable> HorizontalStatsBar(
    stats: List<Stat<T>>
) {
    val totalValue = remember(stats) {
        stats.map { it.value }.sum()
    }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scope = rememberCoroutineScope()

    Column {
        LazyRow(
            contentPadding = PaddingValues(8.dp)
        ) {
            items(stats) {
                val percentage = remember(totalValue) {
                    (it.value * 100 / totalValue).format()
                }
                StatChip(
                    stat = it,
                    tooltipText = "$percentage%",
                    scope = scope,
                )
            }
        }

        Row {
            stats.forEach {
                Rectangle(
                    width = (it.value / totalValue * screenWidth).dp,
                    height = 16.dp,
                    color = it.type.primaryColor()
                )
            }
        }

        Text(
            text = stringResource(
                id = R.string.total_entries,
                totalValue.format() ?: totalValue.toString()
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun HorizontalStatsBarPreview() {
    MoeListTheme {
        Surface {
            HorizontalStatsBar(
                stats = Stat.exampleStats
            )
        }
    }
}