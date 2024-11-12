package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.NumExtensions.format

//MAL app style. its easier to see poster on phoneScreen.

//consts for 2perRow
const val multi =1.7;
const val MEDIA_POSTER_COMPACT_HEIGHT_2pr = 100*multi
const val MEDIA_POSTER_COMPACT_WIDTH_2pr = 100*multi

const val MEDIA_POSTER_SMALL_HEIGHT_2pr = 140*multi
const val MEDIA_POSTER_SMALL_WIDTH_2pr = 100*multi

const val MEDIA_POSTER_MEDIUM_HEIGHT_2pr = 156*multi
const val MEDIA_POSTER_MEDIUM_WIDTH_2pr = 110*multi

const val MEDIA_POSTER_BIG_HEIGHT_2pr = 213*multi
const val MEDIA_POSTER_BIG_WIDTH_2pr = 150*multi

const val MEDIA_ITEM_VERTICAL_HEIGHT_2pr = 200*multi



@Composable
fun MediaItemVertical_2perRow(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    subtitle: @Composable (() -> Unit)? = null,
    subtitle2: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(MEDIA_POSTER_SMALL_WIDTH_2pr.dp)
            //.width(300.dp)
            .sizeIn(
                minHeight = MEDIA_ITEM_VERTICAL_HEIGHT_2pr.dp -100.dp
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /*use box to overlay*/
        //val shape =  RoundedCornerShape(8.dp)
        Box(
            modifier = modifier
                //.background(Color.Red )
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        )
        {
            //layer1
            MediaPoster(
                url = imageUrl,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_MEDIUM_WIDTH_2pr.dp ,
                        height = MEDIA_POSTER_MEDIUM_HEIGHT_2pr.dp
                    )
            )
            //layer2
            Column(
                modifier = modifier
                    .offset(x=5.dp ,y= -14.dp)
                    .background(Color.DarkGray)
            )
            {
                subtitle?.let { it() }
                subtitle2?.let { it() }
            }

        }


        Text(
            text = title,
            modifier = Modifier
                .width(MEDIA_POSTER_SMALL_WIDTH_2pr.dp)
                .padding(top = 2.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            minLines = minLines
        )


    }
}



@Composable
fun MediaItemVertical_2perRowPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .size(
                width = (MEDIA_POSTER_SMALL_WIDTH_2pr + 8).dp,
                height = MEDIA_ITEM_VERTICAL_HEIGHT_2pr.dp
            )
            .padding(end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_SMALL_WIDTH_2pr.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT_2pr.dp
                )
                .defaultPlaceholder(visible = true)
        )

        Text(
            text = "This is a placeholder",
            modifier = Modifier
                .padding(top = 8.dp)
                .defaultPlaceholder(visible = true),
            fontSize = 15.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

@Preview
@Composable
fun MediaItemVertical_2perRowPreview() {
    MoeListTheme {
        Surface {
            val _textColor = Color(200,200,200);

            MediaItemVertical_2perRow (
                imageUrl = null,
                title = "This is a very large anime title that should serve as a preview example",
                subtitle = {
                    SmallScoreIndicator(
                        score = 8.34f,
                        fontSize = 14.sp,
                        textColor = _textColor,
                    )
                },
                subtitle2 = {
                        TextIconHorizontal(
                            text = "222.123",
                            icon = R.drawable.ic_round_group_24,
                            color = _textColor,
                            fontSize = 13.sp,
                            iconSize = 16.dp
                        )
                },
                onClick = {}
            )
        }
    }
}