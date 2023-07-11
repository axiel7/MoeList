package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@Composable
fun HorizontalStatsBar(
    stats: State<List<Stat>>
) {
    val totalValue by remember {
        derivedStateOf { stats.value.map { it.value }.sum() }
    }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    Column {
        LazyRow(
            contentPadding = PaddingValues(8.dp)
        ) {
            items(stats.value) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(text = stringResource(it.title)) },
                    modifier = Modifier.padding(end = 8.dp),
                    icon = { Text(text = String.format("%.0f", it.value)) },
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = it.color
                    )
                )
            }
        }

        Row {
            stats.value.forEach {
                Rectangle(
                    width = (it.value / totalValue * screenWidth).dp,
                    height = 16.dp,
                    color = it.color
                )
            }
        }

        Text(
            text = stringResource(R.string.total_entries).format(String.format("%.0f", totalValue)),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalStatsBarPreview() {
    val stats = remember {
        mutableStateOf(
            listOf(
                Stat(
                    title = R.string.watching,
                    value = 12f,
                    color = Color(red = 0, green = 200, blue = 83)
                ),
                Stat(
                    title = R.string.completed,
                    value = 420f,
                    color = Color(red = 92, green = 107, blue = 192)
                ),
                Stat(
                    title = R.string.on_hold,
                    value = 5f,
                    color = Color(red = 255, green = 213, blue = 0)
                ),
                Stat(
                    title = R.string.dropped,
                    value = 3f,
                    color = Color(red = 213, green = 0, blue = 0)
                ),
                Stat(
                    title = R.string.ptw,
                    value = 30f,
                    color = Color(red = 158, green = 158, blue = 158)
                ),
            )
        )
    }
    MoeListTheme {
        HorizontalStatsBar(
            stats = stats
        )
    }
}