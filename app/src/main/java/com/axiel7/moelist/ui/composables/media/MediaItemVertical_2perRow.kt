package com.axiel7.moelist.ui.composables.media

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
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
import androidx.compose.ui.platform.LocalConfiguration
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

//MAL app style. 2perRow - its easier to see poster on phoneScreen.
const val multi =2.0;
const val MEDIA_POSTER_COMPACT_HEIGHT_2pr = 100*multi
const val MEDIA_POSTER_COMPACT_WIDTH_2pr = 100*multi

const val MEDIA_POSTER_SMALL_HEIGHT_2pr = 140*multi
const val MEDIA_POSTER_SMALL_WIDTH_2pr = 100*multi  +20 //+100 see if img is centered

const val MEDIA_POSTER_MEDIUM_HEIGHT_2pr = 156*multi
const val MEDIA_POSTER_MEDIUM_WIDTH_2pr = 110*multi

const val MEDIA_POSTER_BIG_HEIGHT_2pr = 213*multi
const val MEDIA_POSTER_BIG_WIDTH_2pr = 150*multi

const val MEDIA_ITEM_VERTICAL_HEIGHT_2pr = (156+25)*multi

//val Teal200 = Color(0xFF03DAC5)
val DarkTheme_textColor = Color(220, 220, 220)


@Composable
fun getGridCellFixed_Count_ForOrientation():Int
{
    val orient = LocalConfiguration.current.orientation
    val landScape = Configuration.ORIENTATION_LANDSCAPE

    val count = if(orient == landScape ) 3 else 2
    return  count
}


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
                minHeight = MEDIA_ITEM_VERTICAL_HEIGHT_2pr.dp -60.dp // -100.dp
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*Poster And StartCount Container - use box to overlay*/
        Box(
            modifier = modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        )
        {
            //layer1
            MediaPoster(
                url = imageUrl,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_MEDIUM_WIDTH_2pr.dp  ,
                        height = MEDIA_POSTER_MEDIUM_HEIGHT_2pr.dp -50.dp
                    ),
            )
            //layer2 - StartCount -PeopleCount
            Column(
                modifier = modifier
                    .offset(x=4.dp ,y= -15.dp)
                    .background(Color.DarkGray)
                    .padding(vertical = 3.dp, horizontal = 0.dp),
//                horizontalAlignment = Alignment.Start,
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
                .padding(top = 0.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            minLines = minLines
        )


    }
}



@Preview // for debug
@Composable
fun MediaItemVertical_2perRowPlaceholder(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .size(
                width = (MEDIA_POSTER_SMALL_WIDTH_2pr ).dp,
                height = MEDIA_ITEM_VERTICAL_HEIGHT_2pr.dp -60.dp
            )
            //.padding(end = 8.dp ),
            .padding(start = 8.dp,end = 8.dp, top=4.dp ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
//                    width = MEDIA_POSTER_SMALL_WIDTH_2pr.dp,
//                    height = MEDIA_POSTER_SMALL_HEIGHT_2pr.dp +14.dp
                    width = MEDIA_POSTER_MEDIUM_WIDTH_2pr.dp,
                    height = MEDIA_POSTER_MEDIUM_HEIGHT_2pr.dp -6.dp -50.dp
                )
                .padding( bottom=4.dp )
                .defaultPlaceholder(visible = true)
        )

        Text(
            text = "This is a placeholder - i need to make it 2 lines  ",
            modifier = Modifier
                .padding(top = 4.dp)
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
            MediaItemVertical_2perRow (
                imageUrl = null,
                title = "This is a very large anime title that should serve as a preview example",
                subtitle = {
                    SmallScoreIndicator(
                        score = 8.55f,
                        textColor = DarkTheme_textColor,
                        fontSize = 15.sp,
                        lineHeight =  16.sp,
                        iconPaddingEnd= 4.dp,
                    )
                },
                subtitle2 = {
                        TextIconHorizontal(
                            text = "200.555",
                            icon = R.drawable.ic_round_group_24,
                            color = DarkTheme_textColor,
                            fontSize = 13.sp,
                            iconSize = 16.dp,
                            lineHeight =  16.sp,
                        )
                },
                onClick = {}
            )
        }
    }
}