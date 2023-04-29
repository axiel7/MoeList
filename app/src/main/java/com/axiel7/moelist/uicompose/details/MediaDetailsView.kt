package com.axiel7.moelist.uicompose.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.*
import com.axiel7.moelist.uicompose.composables.*
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.ContextExtensions.getCurrentLanguageTag
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.ContextExtensions.openInGoogleTranslate
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.DateUtils.deduceDateFormat
import com.axiel7.moelist.utils.DateUtils.parseDate
import com.axiel7.moelist.utils.DateUtils.toLocalized
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.StringExtensions.toNavArgument
import com.axiel7.moelist.utils.UseCases.copyToClipBoard
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.coroutines.launch

const val MEDIA_DETAILS_DESTINATION = "details/{mediaType}/{mediaId}"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var maxLinesSynopsis by remember { mutableStateOf(5) }
    var iconExpand by remember { mutableStateOf(R.drawable.ic_round_keyboard_arrow_down_24) }
    val isNewEntry by remember {
        derivedStateOf { viewModel.mediaDetails?.myListStatus == null }
    }
    val isCurrentLanguageEn = remember { getCurrentLanguageTag()?.startsWith("en") }

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
                if (viewModel.mediaDetails != null) coroutineScope.launch { sheetState.show() }
            }) {
                Icon(
                    painter = painterResource(if (isNewEntry) R.drawable.ic_round_add_24
                            else R.drawable.ic_round_edit_24),
                    contentDescription = "edit"
                )
                Text(
                    text = stringResource(if (isNewEntry) R.string.add else R.string.edit),
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(bottom = 68.dp)
        ) {
            Row {
                MediaPoster(
                    url = viewModel.mediaDetails?.mainPicture?.large,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .size(
                            width = MEDIA_POSTER_BIG_WIDTH.dp,
                            height = MEDIA_POSTER_BIG_HEIGHT.dp
                        )
                        .placeholder(visible = viewModel.isLoading)
                        .clickable {
                            navController.navigate(
                                "full_poster/${viewModel.picturesUrls.toNavArgument()}"
                            )
                        }
                )
                Column {
                    Text(
                        text = viewModel.mediaDetails?.title ?: "Loading",
                        modifier = Modifier
                            .padding(bottom = 8.dp, end = 8.dp)
                            .placeholder(visible = viewModel.isLoading)
                            .combinedClickable(
                                onLongClick = {
                                    viewModel.mediaDetails?.title?.let { context.copyToClipBoard(it) }
                                },
                                onClick = { }
                            ),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextIconHorizontal(
                        text = viewModel.mediaDetails?.mediaType?.mediaFormatLocalized() ?: "Loading",
                        icon = if (mediaType == MediaType.ANIME) R.drawable.ic_round_movie_24
                        else R.drawable.ic_round_menu_book_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.mediaDetails?.durationText() ?: "Loading",
                        icon = R.drawable.ic_round_timer_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.mediaDetails?.status?.statusLocalized() ?: "Loading",
                        icon = R.drawable.ic_round_rss_feed_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.mediaDetails?.mean.toString(),
                        icon = R.drawable.ic_round_details_star_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                }
            }//:Row

            //Genres
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                viewModel.mediaDetails?.genres?.let { genres ->
                    items(genres) {
                        AssistChip(
                            onClick = { },
                            label = { Text(text = it.nameLocalized()) },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            //Synopsis
            Text(
                text = viewModel.mediaDetails?.synopsis ?: stringResource(R.string.lorem_ipsun),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .placeholder(visible = viewModel.isLoading),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLinesSynopsis
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentLanguageEn == false) {
                    IconButton(onClick = {
                        viewModel.mediaDetails?.synopsis?.let { context.openInGoogleTranslate(it) }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_outline_translate_24),
                            contentDescription = stringResource(R.string.translate)
                        )
                    }
                }
                else Spacer(modifier = Modifier.size(48.dp))

                IconButton(
                    onClick = {
                        if (maxLinesSynopsis == 5) {
                            maxLinesSynopsis = Int.MAX_VALUE
                            iconExpand = R.drawable.ic_round_keyboard_arrow_up_24
                        } else {
                            maxLinesSynopsis = 5
                            iconExpand = R.drawable.ic_round_keyboard_arrow_down_24
                        }
                    }
                ) {
                    Icon(painter = painterResource(iconExpand), contentDescription = "expand")
                }

                IconButton(
                    onClick = {
                        viewModel.mediaDetails?.synopsis?.let { context.copyToClipBoard(it) }
                    }
                ) {
                    Icon(painter = painterResource(R.drawable.round_content_copy_24), contentDescription = "copy")
                }
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
                    text = viewModel.mediaDetails?.rankText() ?: "",
                    icon = R.drawable.ic_round_bar_chart_24,
                    tooltip = stringResource(R.string.top_ranked)
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = App.numberFormat.format(
                        viewModel.mediaDetails?.numScoringUsers ?: 0
                    ),
                    icon = R.drawable.ic_round_thumbs_up_down_24,
                    tooltip = stringResource(R.string.users_scores)
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = App.numberFormat.format(viewModel.mediaDetails?.numListUsers ?: 0),
                    icon = R.drawable.ic_round_group_24,
                    tooltip = stringResource(R.string.members)
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = "# ${viewModel.mediaDetails?.popularity}",
                    icon = R.drawable.ic_round_trending_up_24,
                    tooltip = stringResource(R.string.popularity)
                )
            }//:Row

            //Info
            InfoTitle(text = stringResource(R.string.more_info))
            if (mediaType == MediaType.MANGA) {
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.authors),
                        info = (viewModel.mediaDetails as? MangaDetails)?.authors
                            ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                        modifier = Modifier.placeholder(visible = viewModel.isLoading)
                    )
                }
                MediaInfoView(
                    title = stringResource(R.string.volumes),
                    info = (viewModel.mediaDetails as? MangaDetails)?.numVolumes.toStringPositiveValueOrNull(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            viewModel.mediaDetails?.synonymsJoined()?.let { synonyms ->
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.synonyms),
                        info = synonyms,
                        modifier = Modifier.placeholder(visible = viewModel.isLoading)
                    )
                }
            }
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(R.string.jp_title),
                    info = viewModel.mediaDetails?.alternativeTitles?.ja,
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            MediaInfoView(
                title = stringResource(R.string.start_date),
                info = viewModel.mediaDetails?.startDate?.let {
                    it.parseDate(
                        inputFormat = it.deduceDateFormat()
                    ).toLocalized()
                },
                modifier = Modifier.placeholder(visible = viewModel.isLoading)
            )
            MediaInfoView(
                title = stringResource(R.string.end_date),
                info = viewModel.mediaDetails?.endDate?.let {
                    it.parseDate(
                        inputFormat = it.deduceDateFormat()
                    ).toLocalized()
                },
                modifier = Modifier.placeholder(visible = viewModel.isLoading)
            )
            if (mediaType == MediaType.ANIME) {
                MediaInfoView(
                    title = stringResource(R.string.season),
                    info = (viewModel.mediaDetails as? AnimeDetails)?.startSeason.seasonYearText(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.broadcast),
                    info = (viewModel.mediaDetails as? AnimeDetails)?.broadcastTimeText(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.duration),
                    info = (viewModel.mediaDetails as? AnimeDetails)?.episodeDurationLocalized(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.source),
                    info = (viewModel.mediaDetails as? AnimeDetails)?.sourceLocalized(),
                    modifier = Modifier.placeholder(visible = viewModel.isLoading)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(
                        id = if (mediaType == MediaType.ANIME) R.string.studios else R.string.serialization
                    ),
                    info = viewModel.studioSerializationJoined,
                    modifier = Modifier
                        .placeholder(visible = viewModel.isLoading)
                        .padding(bottom = 8.dp)
                )
            }

            //Themes
            if (mediaType == MediaType.ANIME) {
                (viewModel.mediaDetails as? AnimeDetails)?.openingThemes?.let { themes ->
                    InfoTitle(text = stringResource(R.string.opening))
                    themes.forEach { theme ->
                        AnimeThemeItem(text = theme.text, onClick = {
                            context.openAction(
                                Constants.YOUTUBE_QUERY_URL + viewModel.buildQueryFromThemeText(theme.text)
                            )
                        })
                    }
                }

                (viewModel.mediaDetails as? AnimeDetails)?.endingThemes?.let { themes ->
                    InfoTitle(text = stringResource(R.string.ending))
                    themes.forEach { theme ->
                        AnimeThemeItem(text = theme.text, onClick = {
                            context.openAction(
                                Constants.YOUTUBE_QUERY_URL + viewModel.buildQueryFromThemeText(theme.text)
                            )
                        })
                    }
                }
            }

            //Related
            if (viewModel.relatedAnime.isNotEmpty()) {
                InfoTitle(text = stringResource(R.string.related_anime))
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(viewModel.relatedAnime) { item ->
                        MediaItemVertical(
                            url = item.node.mainPicture?.large,
                            title = item.node.title,
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                navController.navigate("details/ANIME/${item.node.id}")
                            }
                        )
                    }
                }
            }
            if (viewModel.relatedManga.isNotEmpty()) {
                InfoTitle(text = stringResource(R.string.related_manga))
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(viewModel.relatedManga) { item ->
                        MediaItemVertical(
                            url = item.node.mainPicture?.large,
                            title = item.node.title,
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                navController.navigate("details/MANGA/${item.node.id}")
                            }
                        )
                    }
                }
            }

            //Recommendations
            if (viewModel.recommendations.isNotEmpty()) {
                InfoTitle(text = stringResource(R.string.recommendations))
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(viewModel.recommendations) { item ->
                        MediaItemVertical(
                            url = item.node.mainPicture?.large,
                            title = item.node.title,
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                navController.navigate("details/${mediaType.value}/${item.node.id}")
                            }
                        )
                    }
                }
            }
        }//:Column
    }//:Scaffold

    if (sheetState.isVisible) {
        EditMediaSheet(
            coroutineScope = coroutineScope,
            sheetState = sheetState,
            mediaViewModel = viewModel
        )
    }

    LaunchedEffect(Unit) {
        if (viewModel.mediaDetails == null) viewModel.getDetails(mediaId)
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