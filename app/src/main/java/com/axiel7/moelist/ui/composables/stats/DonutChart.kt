package com.axiel7.moelist.ui.composables.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.base.LocalizableAndColorable
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.ui.theme.MoeListTheme
import java.lang.Integer.min

private const val chartDegrees = 340f // circle shape with spacing
private const val startAngle = 100f // start drawing clockwise

/**
 * Modified version of
 * https://github.com/giorgospat/compose-charts
 * @author axiel7, giorgospat
 */
@Stable
@Composable
fun <T : LocalizableAndColorable> DonutChart(
    modifier: Modifier = Modifier,
    stats: List<Stat<T>>,
    centerContent: @Composable () -> Unit = {},
) {
    val totalSum = remember(stats) {
        stats.map { it.value }.sum()
    }

    // calculate each input percentage
    val proportions = remember(stats) {
        stats.map { it.value * 100 / totalSum }
    }

    // calculate each input slice degrees
    val angleProgress = remember(proportions) {
        proportions.map { prop ->
            chartDegrees * prop / 100
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

        val colors = stats.map { it.type.primaryColor() }
        Canvas(
            modifier = Modifier
                .size(canvasSizeDp)
        ) {
            if (totalSum > 0) {
                var start = startAngle
                angleProgress.forEachIndexed { index, angle ->
                    drawArc(
                        color = colors[index],
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

@Preview
@Composable
fun DonutChartPreview() {
    MoeListTheme {
        Surface {
            DonutChart(
                stats = Stat.exampleStats,
                centerContent = {
                    Text(text = "Total: ${Stat.exampleStats.map { it.value }.sum()}")
                }
            )
        }
    }
}