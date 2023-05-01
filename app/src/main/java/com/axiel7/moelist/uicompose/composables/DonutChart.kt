package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import java.lang.Integer.min

private const val chartDegrees = 340f // circle shape with spacing

/**
 * Modified version of
 * https://github.com/giorgospat/compose-charts
 * @author giorgospat
 */
@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    stats: State<List<Stat>>,
    centerContent: @Composable () -> Unit = {},
) {
    // start drawing clockwise
    val startAngle = remember { 100f }

    val totalSum by remember {
        derivedStateOf {
            stats.value.map { it.value }.sum()
        }
    }

    // calculate each input percentage
    val proportions by remember {
        derivedStateOf {
            stats.value.map {
                it.value * 100 / totalSum
            }
        }
    }

    // calculate each input slice degrees
    val angleProgress by remember {
        derivedStateOf {
            proportions.map { prop ->
                chartDegrees * prop / 100
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .size(164.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        val canvasSize = min(constraints.maxWidth, constraints.maxHeight)
        val size = Size(canvasSize.toFloat(), canvasSize.toFloat())
        val canvasSizeDp = with(LocalDensity.current) { canvasSize.toDp() }
        val sliceWidth = with(LocalDensity.current) { 16.dp.toPx() }

        Canvas(
            modifier = Modifier
                .size(canvasSizeDp)
        ) {
            if (totalSum > 0) {
                var start = startAngle
                angleProgress.forEachIndexed { index, angle ->
                    drawArc(
                        color = stats.value[index].color,
                        startAngle = start,
                        sweepAngle = angle,
                        useCenter = false,
                        size = size,
                        style = Stroke(
                            width = sliceWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    start += angle
                }
            }
        }

        centerContent()
    }
}

@Preview(showBackground = true)
@Composable
fun DonutChartPreview() {
    val stats = remember {
        mutableStateOf(listOf(
            Stat(title = R.string.watching, value = 12f, color = Color(red = 0, green = 200, blue = 83)),
            Stat(title = R.string.completed, value = 120f, color = Color(red = 92, green = 107, blue = 192)),
            Stat(title = R.string.on_hold, value = 5f, color = Color(red = 255, green = 213, blue = 0)),
            Stat(title = R.string.dropped, value = 3f, color = Color(red = 213, green = 0, blue = 0)),
            Stat(title = R.string.ptw, value = 30f, color = Color(red = 158, green = 158, blue = 158)),
        ))
    }
    MoeListTheme {
        DonutChart(
            stats = stats,
            centerContent = {
                Text(text = "Total: ${stats.value.map { it.value }.sum()}")
            }
        )
    }
}