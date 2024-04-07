package com.axiel7.moelist.ui.details

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.TextIconVertical
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.stats.HorizontalStatsBar
import com.axiel7.moelist.ui.details.composables.AnimeThemeItem
import com.axiel7.moelist.ui.details.composables.MediaDetailsTopAppBar
import com.axiel7.moelist.ui.details.composables.MediaInfoView
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.CHARACTER_URL
import com.axiel7.moelist.utils.ContextExtensions.copyToClipBoard
import com.axiel7.moelist.utils.ContextExtensions.getCurrentLanguageTag
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions.format
import com.axiel7.moelist.utils.StringExtensions.buildQueryFromThemeText
import com.axiel7.moelist.utils.StringExtensions.toStringOrNull
import com.axiel7.moelist.utils.TranslateUtils.openTranslator
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import com.axiel7.moelist.utils.YOUTUBE_QUERY_URL
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaDetailsView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager
) {
    val viewModel: MediaDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MediaDetailsContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MediaDetailsContent(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    fun hideSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var isSynopsisExpanded by remember { mutableStateOf(false) }
    val maxLinesSynopsis by remember {
        derivedStateOf { if (isSynopsisExpanded) Int.MAX_VALUE else 5 }
    }
    val iconExpand by remember {
        derivedStateOf {
            if (isSynopsisExpanded) R.drawable.ic_round_keyboard_arrow_up_24
            else R.drawable.ic_round_keyboard_arrow_down_24
        }
    }
    val isCurrentLanguageEn = remember { getCurrentLanguageTag()?.startsWith("en") }

    if (showSheet && uiState.mediaInfo != null) {
        EditMediaSheet(
            sheetState = sheetState,
            mediaInfo = uiState.mediaInfo!!,
            myListStatus = uiState.myListStatus,
            bottomPadding = bottomBarPadding,
            onEdited = { status, removed ->
                hideSheet()
                event?.onChangedMyListStatus(status, removed)
            },
            onDismissed = { hideSheet() }
        )
    }

    if (uiState.message != null) {
        LaunchedEffect(uiState.message) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediaDetailsTopAppBar(
                uiState = uiState,
                event = event,
                navigateBack = navActionManager::goBack,
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isLoggedIn && uiState.mediaDetails != null) {
                        showSheet = true
                    } else {
                        context.showToast(context.getString(R.string.please_login_to_use_this_feature))
                    }
                }
            ) {
                Icon(
                    painter = painterResource(
                        if (uiState.isNewEntry) R.drawable.ic_round_add_24
                        else R.drawable.ic_round_edit_24
                    ),
                    contentDescription = "edit"
                )
                Text(
                    text = if (uiState.isNewEntry) stringResource(R.string.add)
                    else uiState.mediaDetails?.myListStatus?.status?.localized()
                        ?: stringResource(R.string.edit),
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
                    url = uiState.mediaDetails?.mainPicture?.large,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .size(
                            width = MEDIA_POSTER_BIG_WIDTH.dp,
                            height = MEDIA_POSTER_BIG_HEIGHT.dp
                        )
                        .defaultPlaceholder(visible = uiState.isLoading)
                        .clickable {
                            navActionManager.toFullPoster(uiState.picturesUrls)
                        }
                )
                Column {
                    TextIconHorizontal(
                        text = uiState.mediaDetails?.mediaFormat?.localized() ?: "Loading",
                        icon = if (uiState.isAnime) R.drawable.ic_round_local_movies_24
                        else R.drawable.ic_round_book_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )
                    TextIconHorizontal(
                        text = uiState.mediaDetails?.status?.localized() ?: "Loading",
                        icon = if (uiState.isAnime) R.drawable.ic_round_rss_feed_24
                        else R.drawable.round_drive_file_rename_outline_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )
                    TextIconHorizontal(
                        text = uiState.mediaDetails?.durationText() ?: "Loading",
                        icon = if (uiState.isAnime) R.drawable.ic_round_timer_24
                        else R.drawable.ic_round_menu_book_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )
                    if (uiState.mediaDetails is MangaDetails) {
                        TextIconHorizontal(
                            text = uiState.mediaDetails.volumesText(),
                            icon = R.drawable.round_bookmark_24,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    if (uiState.mediaDetails is AnimeDetails) {
                        TextIconHorizontal(
                            text = uiState.mediaDetails.startSeason?.year?.toString()
                                ?: stringResource(R.string.unknown),
                            icon = R.drawable.ic_round_event_24,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    TextIconHorizontal(
                        text = uiState.mediaDetails?.mean.toStringOrNull() ?: "??",
                        icon = R.drawable.ic_round_details_star_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
            }//:Row

            // Title
            Text(
                text = uiState.mediaDetails?.userPreferredTitle() ?: "Loading",
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .defaultPlaceholder(visible = uiState.isLoading)
                    .combinedClickable(
                        onLongClick = {
                            uiState.mediaDetails?.title?.let { context.copyToClipBoard(it) }
                        },
                        onClick = { }
                    ),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            //Genres
            LazyRow(
                modifier = Modifier.padding(bottom = 4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(uiState.mediaDetails?.genres.orEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(text = it.localized()) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            //Synopsis
            SelectionContainer {
                Text(
                    text = uiState.mediaDetails?.synopsisAndBackground()
                        ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                        .animateContentSize()
                        .defaultPlaceholder(visible = uiState.isLoading),
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = maxLinesSynopsis
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentLanguageEn == false) {
                    IconButton(
                        onClick = {
                            uiState.mediaDetails?.synopsis?.let { context.openTranslator(it) }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_outline_translate_24),
                            contentDescription = stringResource(R.string.translate)
                        )
                    }
                } else Spacer(modifier = Modifier.size(48.dp))

                IconButton(
                    onClick = { isSynopsisExpanded = !isSynopsisExpanded }
                ) {
                    Icon(painter = painterResource(iconExpand), contentDescription = "expand")
                }

                IconButton(
                    onClick = {
                        uiState.mediaDetails?.synopsis?.let { context.copyToClipBoard(it) }
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
                    .defaultPlaceholder(visible = uiState.isLoading),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextIconVertical(
                    text = uiState.mediaDetails?.rankText().orEmpty(),
                    icon = R.drawable.ic_round_bar_chart_24,
                    tooltip = stringResource(R.string.top_ranked)
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = uiState.mediaDetails?.numScoringUsers?.format() ?: UNKNOWN_CHAR,
                    icon = R.drawable.ic_round_thumbs_up_down_24,
                    tooltip = stringResource(R.string.users_scores)
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = uiState.mediaDetails?.numListUsers?.format() ?: UNKNOWN_CHAR,
                    icon = R.drawable.ic_round_group_24,
                    tooltip = stringResource(R.string.members)
                )
                VerticalDivider(modifier = Modifier.height(32.dp))

                TextIconVertical(
                    text = "# ${uiState.mediaDetails?.popularity}",
                    icon = R.drawable.ic_round_trending_up_24,
                    tooltip = stringResource(R.string.popularity)
                )
            }//:Row

            //Info
            InfoTitle(text = stringResource(R.string.more_info))
            if (uiState.mediaDetails is MangaDetails) {
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.authors),
                        info = uiState.mediaDetails.authors
                            ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.serialization),
                        info = uiState.serializationJoined,
                        modifier = Modifier
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            MediaInfoView(
                title = stringResource(R.string.start_date),
                info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
            )
            MediaInfoView(
                title = stringResource(R.string.end_date),
                info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
            )
            if (uiState.mediaDetails is AnimeDetails) {
                MediaInfoView(
                    title = stringResource(R.string.season),
                    info = uiState.mediaDetails.startSeason?.seasonYearText(),
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.broadcast),
                    info = uiState.mediaDetails.broadcast?.timeText(
                        isAiring = uiState.mediaDetails.status == MediaStatus.AIRING
                    ),
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.source),
                    info = uiState.mediaDetails.source?.localized(),
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (uiState.mediaDetails is AnimeDetails) {
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.studios),
                        info = uiState.studiosJoined,
                        modifier = Modifier
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            uiState.mediaDetails?.synonymsJoined()?.let { synonyms ->
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.synonyms),
                        info = synonyms,
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
            }
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(R.string.jp_title),
                    info = uiState.mediaDetails?.alternativeTitles?.ja,
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
            }
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(R.string.romaji),
                    info = uiState.mediaDetails?.title,
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
            }
            SelectionContainer {
                MediaInfoView(
                    title = stringResource(R.string.english),
                    info = uiState.mediaDetails?.alternativeTitles?.en,
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
            }

            //Characters
            if (uiState.isAnime) {
                var showCharacters by remember { mutableStateOf(false) }

                InfoTitle(text = stringResource(R.string.characters))
                if (showCharacters || uiState.isLoadingCharacters) {
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(
                            items = uiState.characters,
                            contentType = { it }
                        ) { item ->
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.medium,
                                title = item.fullName(),
                                modifier = Modifier.padding(end = 8.dp),
                                subtitle = {
                                    Text(
                                        text = item.role?.localized().orEmpty(),
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 13.sp
                                    )
                                },
                                minLines = 2,
                                onClick = {
                                    context.openLink(CHARACTER_URL + item.node.id)
                                }
                            )
                        }
                        if (uiState.isLoadingCharacters) {
                            item {
                                CircularProgressIndicator()
                            }
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            showCharacters = true
                            event?.getCharacters()
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.view_characters))
                    }
                }
            }

            //Themes
            if (uiState.mediaDetails is AnimeDetails) {
                uiState.mediaDetails.openingThemes?.let { themes ->
                    InfoTitle(text = stringResource(R.string.opening))
                    themes.forEach { theme ->
                        AnimeThemeItem(
                            text = theme.text,
                            onClick = {
                                context.openAction(
                                    YOUTUBE_QUERY_URL + theme.text.buildQueryFromThemeText()
                                )
                            }
                        )
                    }
                }

                uiState.mediaDetails.endingThemes?.let { themes ->
                    InfoTitle(text = stringResource(R.string.ending))
                    themes.forEach { theme ->
                        AnimeThemeItem(
                            text = theme.text,
                            onClick = {
                                context.openAction(
                                    YOUTUBE_QUERY_URL + theme.text.buildQueryFromThemeText()
                                )
                            }
                        )
                    }
                }
            }

            //Related
            if (uiState.relatedAnime.isNotEmpty()) {
                InfoTitle(text = stringResource(R.string.related_anime))
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.relatedAnime) { item ->
                        MediaItemVertical(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.userPreferredTitle(),
                            modifier = Modifier.padding(end = 8.dp),
                            subtitle = {
                                Text(
                                    text = item.relationType.localized(),
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp,
                                    lineHeight = 14.sp
                                )
                            },
                            onClick = {
                                navActionManager.toMediaDetails(MediaType.ANIME, item.node.id)
                            }
                        )
                    }
                }
            }
            if (uiState.relatedManga.isNotEmpty()) {
                InfoTitle(text = stringResource(R.string.related_manga))
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.relatedManga) { item ->
                        MediaItemVertical(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.userPreferredTitle(),
                            modifier = Modifier.padding(end = 8.dp),
                            subtitle = {
                                Text(
                                    text = item.relationType.localized(),
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp,
                                    lineHeight = 14.sp
                                )
                            },
                            onClick = {
                                navActionManager.toMediaDetails(MediaType.MANGA, item.node.id)
                            }
                        )
                    }
                }
            }

            //Recommendations
            if (uiState.recommendations.isNotEmpty()) {
                InfoTitle(text = stringResource(R.string.recommendations))
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.recommendations) { item ->
                        MediaItemVertical(
                            imageUrl = item.node.mainPicture?.large,
                            title = item.node.userPreferredTitle(),
                            modifier = Modifier.padding(end = 8.dp),
                            subtitle = {
                                TextIconHorizontal(
                                    text = item.numRecommendations.format() ?: UNKNOWN_CHAR,
                                    icon = R.drawable.ic_round_thumbs_up_down_16,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp,
                                    iconSize = 20.dp,
                                )
                            },
                            minLines = 2,
                            onClick = {
                                navActionManager.toMediaDetails(
                                    mediaType = item.node.mediaType,
                                    id = item.node.id
                                )
                            }
                        )
                    }
                }
            }

            (uiState.mediaDetails as? AnimeDetails)?.statistics?.status?.toStats()?.let { stats ->
                InfoTitle(text = stringResource(R.string.status_distribution))
                HorizontalStatsBar(
                    stats = stats
                )
            }
        }//:Column
    }//:Scaffold
}

@Preview
@Composable
fun MediaDetailsPreview() {
    MoeListTheme {
        Surface {
            MediaDetailsContent(
                uiState = MediaDetailsUiState(),
                event = null,
                isLoggedIn = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}