package com.axiel7.moelist.uicompose.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.broadcastTimeText
import com.axiel7.moelist.data.model.anime.episodeDurationLocalized
import com.axiel7.moelist.data.model.anime.seasonYearText
import com.axiel7.moelist.data.model.anime.sourceLocalized
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.durationText
import com.axiel7.moelist.data.model.media.mediaFormatLocalized
import com.axiel7.moelist.data.model.media.nameLocalized
import com.axiel7.moelist.data.model.media.rankText
import com.axiel7.moelist.data.model.media.relationLocalized
import com.axiel7.moelist.data.model.media.statusLocalized
import com.axiel7.moelist.data.model.media.synonymsJoined
import com.axiel7.moelist.data.model.media.synopsisAndBackground
import com.axiel7.moelist.data.model.media.userPreferredTitle
import com.axiel7.moelist.uicompose.composables.InfoTitle
import com.axiel7.moelist.uicompose.composables.TextIconHorizontal
import com.axiel7.moelist.uicompose.composables.TextIconVertical
import com.axiel7.moelist.uicompose.composables.defaultPlaceholder
import com.axiel7.moelist.uicompose.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.moelist.uicompose.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.moelist.uicompose.composables.media.MediaItemVertical
import com.axiel7.moelist.uicompose.composables.media.MediaPoster
import com.axiel7.moelist.uicompose.details.composables.AnimeThemeItem
import com.axiel7.moelist.uicompose.details.composables.MediaDetailsTopAppBar
import com.axiel7.moelist.uicompose.details.composables.MediaInfoView
import com.axiel7.moelist.uicompose.editmedia.EditMediaSheet
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.ContextExtensions.getCurrentLanguageTag
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.ContextExtensions.openInGoogleTranslate
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.StringExtensions.toNavArgument
import com.axiel7.moelist.utils.StringExtensions.toStringOrNull
import com.axiel7.moelist.utils.UseCases.copyToClipBoard
import kotlinx.coroutines.launch

const val MEDIA_TYPE_ARGUMENT = "{mediaType}"
const val MEDIA_ID_ARGUMENT = "{mediaId}"
const val MEDIA_DETAILS_DESTINATION = "details/$MEDIA_TYPE_ARGUMENT/$MEDIA_ID_ARGUMENT"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaDetailsView(
    mediaType: MediaType,
    mediaId: Int,
    isLoggedIn: Boolean,
    navigateBack: () -> Unit,
    navigateToMediaDetails: (MediaType, Int) -> Unit,
    navigateToFullPoster: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: MediaDetailsViewModel = viewModel { MediaDetailsViewModel(mediaType) }

    val scrollState = rememberScrollState()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var maxLinesSynopsis by remember { mutableIntStateOf(5) }
    var iconExpand by remember { mutableIntStateOf(R.drawable.ic_round_keyboard_arrow_down_24) }
    val isNewEntry by remember {
        derivedStateOf { viewModel.mediaDetails?.myListStatus == null }
    }
    val isCurrentLanguageEn = remember { getCurrentLanguageTag()?.startsWith("en") }

    if (sheetState.isVisible) {
        EditMediaSheet(
            coroutineScope = coroutineScope,
            sheetState = sheetState,
            mediaViewModel = viewModel,
            bottomPadding = bottomBarPadding
        )
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(mediaId) {
        if (viewModel.mediaDetails == null) viewModel.getDetails(mediaId)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediaDetailsTopAppBar(
                viewModel = viewModel,
                mediaUrl = if (mediaType == MediaType.ANIME) Constants.ANIME_URL + mediaId
                else Constants.MANGA_URL + mediaId,
                navigateBack = navigateBack,
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isLoggedIn) {
                        if (viewModel.mediaDetails != null) coroutineScope.launch { sheetState.show() }
                    } else context.showToast(context.getString(R.string.please_login_to_use_this_feature))
                }
            ) {
                Icon(
                    painter = painterResource(
                        if (isNewEntry) R.drawable.ic_round_add_24
                        else R.drawable.ic_round_edit_24
                    ),
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
                .padding(bottom = 88.dp)
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
                        .defaultPlaceholder(visible = viewModel.isLoading)
                        .clickable {
                            navigateToFullPoster(viewModel.picturesUrls.toNavArgument())
                        }
                )
                Column {
                    Text(
                        text = viewModel.mediaDetails?.userPreferredTitle() ?: "Loading",
                        modifier = Modifier
                            .padding(bottom = 8.dp, end = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading)
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
                        text = viewModel.mediaDetails?.mediaType?.mediaFormatLocalized()
                            ?: "Loading",
                        icon = if (mediaType == MediaType.ANIME) R.drawable.ic_round_movie_24
                        else R.drawable.ic_round_menu_book_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.mediaDetails?.durationText() ?: "Loading",
                        icon = R.drawable.ic_round_timer_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.mediaDetails?.status?.statusLocalized() ?: "Loading",
                        icon = R.drawable.ic_round_rss_feed_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading)
                    )
                    TextIconHorizontal(
                        text = viewModel.mediaDetails?.mean.toStringOrNull() ?: "??",
                        icon = R.drawable.ic_round_details_star_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading)
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
                text = viewModel.mediaDetails?.synopsisAndBackground()
                    ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .defaultPlaceholder(visible = viewModel.isLoading),
                lineHeight = 20.sp,
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
                } else Spacer(modifier = Modifier.size(48.dp))

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
                    Icon(
                        painter = painterResource(R.drawable.round_content_copy_24),
                        contentDescription = "copy"
                    )
                }
            }

            //Stats
            InfoTitle(text = stringResource(R.string.stats))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .defaultPlaceholder(visible = viewModel.isLoading),
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
                    text = NumExtensions.numberFormat.format(
                        viewModel.mediaDetails?.numScoringUsers ?: 0
                    ),
                    icon = R.drawable.ic_round_thumbs_up_down_24,
                    tooltip = stringResource(R.string.users_scores)
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = NumExtensions.numberFormat.format(
                        viewModel.mediaDetails?.numListUsers ?: 0
                    ),
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
                        modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                    )
                }
                MediaInfoView(
                    title = stringResource(R.string.volumes),
                    info = (viewModel.mediaDetails as? MangaDetails)?.numVolumes.toStringPositiveValueOrNull(),
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            viewModel.mediaDetails?.synonymsJoined()?.let { synonyms ->
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.synonyms),
                        info = synonyms,
                        modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                    )
                }
            }
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(R.string.jp_title),
                    info = viewModel.mediaDetails?.alternativeTitles?.ja,
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
            }
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(R.string.romaji),
                    info = viewModel.mediaDetails?.title,
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
            }
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(R.string.english),
                    info = viewModel.mediaDetails?.alternativeTitles?.en,
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            MediaInfoView(
                title = stringResource(R.string.start_date),
                info = viewModel.mediaDetails?.startDate?.parseDateAndLocalize(),
                modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
            )
            MediaInfoView(
                title = stringResource(R.string.end_date),
                info = viewModel.mediaDetails?.endDate?.parseDateAndLocalize(),
                modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
            )
            if (mediaType == MediaType.ANIME) {
                val animeDetails = viewModel.mediaDetails as? AnimeDetails
                MediaInfoView(
                    title = stringResource(R.string.season),
                    info = animeDetails?.startSeason.seasonYearText(),
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.broadcast),
                    info = animeDetails?.broadcastTimeText(),
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.duration),
                    info = animeDetails?.episodeDurationLocalized(),
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.source),
                    info = animeDetails?.sourceLocalized(),
                    modifier = Modifier.defaultPlaceholder(visible = viewModel.isLoading)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(
                        id = if (mediaType == MediaType.ANIME) R.string.studios else R.string.serialization
                    ),
                    info = viewModel.studioSerializationJoined,
                    modifier = Modifier
                        .defaultPlaceholder(visible = viewModel.isLoading)
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
                                Constants.YOUTUBE_QUERY_URL + viewModel.buildQueryFromThemeText(
                                    theme.text
                                )
                            )
                        })
                    }
                }

                (viewModel.mediaDetails as? AnimeDetails)?.endingThemes?.let { themes ->
                    InfoTitle(text = stringResource(R.string.ending))
                    themes.forEach { theme ->
                        AnimeThemeItem(text = theme.text, onClick = {
                            context.openAction(
                                Constants.YOUTUBE_QUERY_URL + viewModel.buildQueryFromThemeText(
                                    theme.text
                                )
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
                            title = item.node.userPreferredTitle(),
                            modifier = Modifier.padding(end = 8.dp),
                            subtitle = {
                                Text(
                                    text = item.relationLocalized(),
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                navigateToMediaDetails(MediaType.ANIME, item.node.id)
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
                            title = item.node.userPreferredTitle(),
                            modifier = Modifier.padding(end = 8.dp),
                            subtitle = {
                                Text(
                                    text = item.relationLocalized(),
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                navigateToMediaDetails(MediaType.MANGA, item.node.id)
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
                            title = item.node.userPreferredTitle(),
                            modifier = Modifier.padding(end = 8.dp),
                            subtitle = {
                                TextIconHorizontal(
                                    text = NumExtensions.numberFormat.format(item.numRecommendations),
                                    icon = R.drawable.ic_round_thumbs_up_down_16,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                navigateToMediaDetails(mediaType, item.node.id)
                            }
                        )
                    }
                }
            }
        }//:Column
    }//:Scaffold
}

@Preview(showBackground = true)
@Composable
fun MediaDetailsPreview() {
    MoeListTheme {
        MediaDetailsView(
            mediaType = MediaType.ANIME,
            mediaId = 1,
            isLoggedIn = false,
            navigateBack = {},
            navigateToMediaDetails = { _, _ -> },
            navigateToFullPoster = {}
        )
    }
}