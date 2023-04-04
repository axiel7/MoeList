package com.axiel7.moelist.uicompose.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.*
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.composables.*
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.Extensions.openAction
import com.axiel7.moelist.utils.Extensions.openLink
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.coroutines.launch

const val MEDIA_DETAILS_DESTINATION = "details/{mediaType}/{mediaId}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailsView(
    mediaType: MediaType,
    mediaId: Int,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: MediaDetailsViewModel = viewModel { MediaDetailsViewModel(mediaType) }

    val scrollState = rememberScrollState()
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var maxLinesSynopsis by remember { mutableStateOf(5) }
    var iconExpand by remember { mutableStateOf(R.drawable.ic_round_keyboard_arrow_down_24) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediaDetailsTopAppBar(
                navController = navController,
                scrollBehavior = topAppBarScrollBehavior,
                onClickMenu = {
                    if (mediaType == MediaType.ANIME)
                        context.openLink(Constants.ANIME_URL + mediaId)
                    else context.openLink(Constants.MANGA_URL + mediaId)
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                coroutineScope.launch { sheetState.expand() }
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_edit_24),
                    contentDescription = "edit"
                )
                Text(
                    text = stringResource(R.string.edit),
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
                .padding(bottom = 64.dp)
        ) {
            Row {
                MediaPoster(
                    url = viewModel.basicDetails?.mainPicture?.large,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(
                            width = MEDIA_POSTER_BIG_WIDTH.dp,
                            height = MEDIA_POSTER_BIG_HEIGHT.dp
                        )
                        .placeholder(visible = viewModel.isLoading)
                )
                Column {
                    Text(
                        text = viewModel.basicDetails?.title ?: "Loading",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .placeholder(visible = viewModel.isLoading),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextIconHorizontal(
                        text = viewModel.basicDetails?.mediaType?.mediaFormatLocalized() ?: "Loading",
                        icon = if (mediaType == MediaType.ANIME) R.drawable.ic_round_movie_24
                        else R.drawable.ic_round_menu_book_24,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = if (mediaType == MediaType.ANIME)
                            viewModel.animeDetails?.durationText() ?: "Loading"
                        else viewModel.mangaDetails?.durationText() ?: "Loading",
                        icon = R.drawable.ic_round_timer_24,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.basicDetails?.status?.statusLocalized() ?: "Loading",
                        icon = R.drawable.ic_round_rss_feed_24,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.basicDetails?.mean.toString(),
                        icon = R.drawable.ic_round_details_star_24,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                }
            }//:Row

            //Synopsis
            Text(
                text = viewModel.basicDetails?.synopsis ?: stringResource(R.string.lorem_ipsun),
                modifier = Modifier
                    .padding(8.dp)
                    .placeholder(visible = viewModel.isLoading),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLinesSynopsis
            )
            IconButton(
                onClick = {
                    if (maxLinesSynopsis == 5) {
                        maxLinesSynopsis = Int.MAX_VALUE
                        iconExpand = R.drawable.ic_round_keyboard_arrow_up_24
                    } else {
                        maxLinesSynopsis = 5
                        iconExpand = R.drawable.ic_round_keyboard_arrow_down_24
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painter = painterResource(iconExpand), contentDescription = "expand")
            }

            //Stats
            InfoTitle(text = stringResource(R.string.stats))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .placeholder(visible = viewModel.isLoading),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextIconVertical(
                    text = viewModel.basicDetails?.rankText() ?: "",
                    icon = R.drawable.ic_round_bar_chart_24,
                    modifier = Modifier.clickable { }
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = App.numberFormat.format(
                        viewModel.basicDetails?.numScoringUsers ?: 0
                    ),
                    icon = R.drawable.ic_round_thumbs_up_down_24,
                    modifier = Modifier.clickable { }
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = App.numberFormat.format(viewModel.basicDetails?.numListUsers ?: 0),
                    icon = R.drawable.ic_round_group_24,
                    modifier = Modifier.clickable { }
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = "# ${viewModel.basicDetails?.popularity}",
                    icon = R.drawable.ic_round_trending_up_24,
                    modifier = Modifier.clickable { }
                )
            }//:Row

            //Info
            InfoTitle(text = stringResource(R.string.more_info))
            if (mediaType == MediaType.MANGA) {
                MediaInfoView(
                    title = stringResource(R.string.authors),
                    info = "Inio Asano",
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.volumes),
                    info = "13",
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            MediaInfoView(
                title = stringResource(R.string.synonyms),
                info = viewModel.basicDetails?.synonymsJoined(),
                modifier = Modifier.placeholder(visible = viewModel.isLoading)
            )
            MediaInfoView(
                title = stringResource(R.string.jp_title),
                info = viewModel.basicDetails?.alternativeTitles?.ja,
                modifier = Modifier.placeholder(visible = viewModel.isLoading)
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            MediaInfoView(
                title = stringResource(R.string.start_date),
                info = viewModel.basicDetails?.startDate,
                modifier = Modifier.placeholder(visible = viewModel.isLoading)
            )
            MediaInfoView(
                title = stringResource(R.string.end_date),
                info = viewModel.basicDetails?.endDate,
                modifier = Modifier.placeholder(visible = viewModel.isLoading)
            )
            if (mediaType == MediaType.ANIME) {
                MediaInfoView(
                    title = stringResource(R.string.season),
                    info = viewModel.animeDetails?.seasonYearText(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.broadcast),
                    info = viewModel.animeDetails?.broadcastTimeText(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.duration),
                    info = viewModel.animeDetails?.episodeDurationLocalized(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.source),
                    info = viewModel.animeDetails?.sourceLocalized(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            MediaInfoView(
                title = stringResource(
                    id = if (mediaType == MediaType.ANIME) R.string.studios else R.string.serialization
                ),
                info = "Toei Animation",
                modifier = Modifier.placeholder(visible = viewModel.isLoading)
            )

            //Themes
            if (mediaType == MediaType.ANIME) {
                InfoTitle(text = stringResource(R.string.opening))
                viewModel.animeDetails?.openingThemes?.forEach { theme ->
                    AnimeThemeItem(text = theme.text, onClick = {
                        context.openAction(
                            Constants.YOUTUBE_QUERY_URL
                                    + viewModel.buildQueryFromThemeText(theme.text)
                        )
                    })
                }

                InfoTitle(text = stringResource(R.string.ending))
                viewModel.animeDetails?.endingThemes?.forEach { theme ->
                    AnimeThemeItem(text = theme.text, onClick = {
                        context.openAction(
                            Constants.YOUTUBE_QUERY_URL
                                    + viewModel.buildQueryFromThemeText(theme.text)
                        )
                    })
                }
            }

            //Related
            InfoTitle(text = stringResource(R.string.relateds))
            LazyRow {
                items(viewModel.related) { item ->
                    MediaItemVertical(
                        url = item.node.mainPicture?.large,
                        title = item.node.title,
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            val type = if (item.isManga()) "MANGA" else "ANIME"
                            navController.navigate("details/$type/${item.node.id}")
                        }
                    )
                }
            }
        }//:Column
    }//:Scaffold

    if (sheetState.isVisible) {
        EditMediaSheet(
            coroutineScope = coroutineScope,
            sheetState = sheetState,
            viewModel = viewModel
        )
    }

    LaunchedEffect(Unit) {
        viewModel.getDetails(mediaId)
    }
}

@Composable
fun AnimeThemeItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}

@Composable
fun MediaInfoView(
    title: String,
    info: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(modifier)
    ) {
        Text(title,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = info ?: stringResource(R.string.unknown),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun InfoTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailsTopAppBar(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickMenu: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(R.string.title_details)) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "back")
            }
        },
        actions = {
            IconButton(onClick = onClickMenu) {
                Icon(
                    painter = painterResource(R.drawable.ic_open_in_browser),
                    contentDescription = stringResource(R.string.view_on_mal)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Preview(showBackground = true)
@Composable
fun MediaDetailsPreview() {
    MoeListTheme {
        MediaDetailsView(
            mediaType = MediaType.ANIME,
            mediaId = 1,
            navController = rememberNavController()
        )
    }
}