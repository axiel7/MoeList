package com.axiel7.moelist.uicompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import coil.compose.AsyncImage
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_LIST_SORT_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LAST_TAB_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LIST_DISPLAY_MODE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_LIST_SORT_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.NSFW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.PROFILE_PICTURE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.START_TAB_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.THEME_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.data.datastore.PreferencesDataStore.getValueSync
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.base.BottomDestination
import com.axiel7.moelist.uicompose.base.BottomDestination.Companion.toBottomDestinationIndex
import com.axiel7.moelist.uicompose.base.ListMode
import com.axiel7.moelist.uicompose.base.StringArrayNavType
import com.axiel7.moelist.uicompose.calendar.CALENDAR_DESTINATION
import com.axiel7.moelist.uicompose.calendar.CalendarView
import com.axiel7.moelist.uicompose.details.FULL_POSTER_DESTINATION
import com.axiel7.moelist.uicompose.details.FullPosterView
import com.axiel7.moelist.uicompose.details.MEDIA_DETAILS_DESTINATION
import com.axiel7.moelist.uicompose.details.MediaDetailsView
import com.axiel7.moelist.uicompose.home.HOME_DESTINATION
import com.axiel7.moelist.uicompose.home.HomeView
import com.axiel7.moelist.uicompose.login.LoginActivity
import com.axiel7.moelist.uicompose.more.ABOUT_DESTINATION
import com.axiel7.moelist.uicompose.more.AboutView
import com.axiel7.moelist.uicompose.more.CREDITS_DESTINATION
import com.axiel7.moelist.uicompose.more.CreditsView
import com.axiel7.moelist.uicompose.more.MORE_DESTINATION
import com.axiel7.moelist.uicompose.more.MoreView
import com.axiel7.moelist.uicompose.more.NOTIFICATIONS_DESTINATION
import com.axiel7.moelist.uicompose.more.NotificationsView
import com.axiel7.moelist.uicompose.more.SETTINGS_DESTINATION
import com.axiel7.moelist.uicompose.more.SettingsView
import com.axiel7.moelist.uicompose.profile.PROFILE_DESTINATION
import com.axiel7.moelist.uicompose.profile.ProfileView
import com.axiel7.moelist.uicompose.ranking.MEDIA_RANKING_DESTINATION
import com.axiel7.moelist.uicompose.ranking.MediaRankingView
import com.axiel7.moelist.uicompose.search.SearchView
import com.axiel7.moelist.uicompose.season.SEASON_CHART_DESTINATION
import com.axiel7.moelist.uicompose.season.SeasonChartView
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.uicompose.userlist.ANIME_LIST_DESTINATION
import com.axiel7.moelist.uicompose.userlist.MANGA_LIST_DESTINATION
import com.axiel7.moelist.uicompose.userlist.UserMediaListHostView
import com.axiel7.moelist.utils.NumExtensions.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        //Cache DataStore in memory
        lifecycleScope.launch {
            defaultPreferencesDataStore.data.first()
        }
        //get necessary preferences while on splashscreen

        val token = defaultPreferencesDataStore.getValueSync(ACCESS_TOKEN_PREFERENCE_KEY)
        if (token != null) App.createKtorClient(token)
        else {
            Intent(this, LoginActivity::class.java).apply { startActivity(this) }
            finish()
        }

        val startTab = defaultPreferencesDataStore.getValueSync(START_TAB_PREFERENCE_KEY)
        var lastTabOpened = intent.action?.toBottomDestinationIndex() ?: startTab?.toBottomDestinationIndex()
        var mediaId: Int? = null
        var mediaType: String? = null
        if (intent.action == "details") {
            mediaId = intent.getIntExtra("media_id", 0)
            mediaType = intent.getStringExtra("media_type")
        }
        if (lastTabOpened == null) {
            lastTabOpened = defaultPreferencesDataStore.getValueSync(LAST_TAB_PREFERENCE_KEY)
        }
        else { // opened from intent or start tab setting
            CoroutineScope(Dispatchers.IO).launch {
                defaultPreferencesDataStore.edit {
                    it[LAST_TAB_PREFERENCE_KEY] = lastTabOpened
                }
            }
        }

        defaultPreferencesDataStore.getValueSync(NSFW_PREFERENCE_KEY)?.let {
            App.nsfw = it.toInt()
        }
        val theme = defaultPreferencesDataStore.getValueSync(THEME_PREFERENCE_KEY) ?: "follow_system"
        defaultPreferencesDataStore.getValueSync(ANIME_LIST_SORT_PREFERENCE_KEY)?.let {
            MediaSort.forValue(it)?.let { sort -> App.animeListSort = sort }
        }
        defaultPreferencesDataStore.getValueSync(MANGA_LIST_SORT_PREFERENCE_KEY)?.let {
            MediaSort.forValue(it)?.let { sort -> App.mangaListSort = sort }
        }
        defaultPreferencesDataStore.getValueSync(LIST_DISPLAY_MODE_PREFERENCE_KEY)?.let {
            ListMode.forValue(it)?.let { mode -> App.listDisplayMode = mode }
        }

        setContent {
            val themePreference by rememberPreference(THEME_PREFERENCE_KEY, theme)
            val navController = rememberNavController()

            MoeListTheme(
                darkTheme = if (themePreference == "follow_system") isSystemInDarkTheme()
                else themePreference == "dark" || themePreference == "black",
                amoledColors = themePreference == "black"
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(
                        navController = navController,
                        lastTabOpened = lastTabOpened ?: 0
                    )
                }
            }

            LaunchedEffect(mediaId) {
                if (mediaId != null && mediaId != 0) {
                    navController.navigate("details/$mediaType/$mediaId")
                }
            }
        }
    }
}

@Composable
fun MainView(
    navController: NavHostController,
    lastTabOpened: Int
) {
    val bottomBarState = remember { mutableStateOf(true) }
    val stringArrayType = remember { StringArrayNavType() }

    com.google.accompanist.insets.ui.Scaffold(
        topBar = {
            MainTopAppBar(
                bottomBarState = bottomBarState,
                navController = navController,
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                bottomBarState = bottomBarState,
                lastTabOpened = lastTabOpened
            )
        },
        backgroundColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val topPadding by animateDpAsState(
            targetValue = padding.calculateTopPadding(),
            label = "top_bar_padding"
        )
        val bottomPadding by animateDpAsState(
            targetValue = padding.calculateBottomPadding(),
            label = "bottom_bar_padding"
        )
        NavHost(
            navController = navController,
            startDestination = when (lastTabOpened) {
                1 -> ANIME_LIST_DESTINATION
                2 -> MANGA_LIST_DESTINATION
                3 -> MORE_DESTINATION
                else -> HOME_DESTINATION
            },
            modifier = Modifier.padding(
                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                end = padding.calculateEndPadding(LocalLayoutDirection.current),
            ),
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(400)) },
            popEnterTransition = { fadeIn(tween(400)) },
            popExitTransition = { fadeOut(tween(400)) }
        ) {
            composable(HOME_DESTINATION) {
                HomeView(
                    navController = navController,
                    modifier = Modifier
                        .padding(top = topPadding, bottom = bottomPadding),
                )
            }

            composable(MEDIA_RANKING_DESTINATION,
                arguments = listOf(navArgument("mediaType") { type = NavType.StringType })
            ) { navEntry ->
                MediaRankingView(
                    mediaType = MediaType.valueOf(navEntry.arguments?.getString("mediaType") ?: "ANIME"),
                    navController = navController
                )
            }

            composable(CALENDAR_DESTINATION) { CalendarView(navController = navController) }

            composable(SEASON_CHART_DESTINATION) { SeasonChartView(navController = navController) }

            composable(ANIME_LIST_DESTINATION) {
                UserMediaListHostView(
                    mediaType = MediaType.ANIME,
                    navController = navController,
                    modifier = Modifier.padding(top = topPadding, bottom = bottomPadding),
                )
            }

            composable(MANGA_LIST_DESTINATION) {
                UserMediaListHostView(
                    mediaType = MediaType.MANGA,
                    navController = navController,
                    modifier = Modifier.padding(top = topPadding, bottom = bottomPadding),
                )
            }

            composable(MORE_DESTINATION) {
                MoreView(
                    navController = navController,
                    modifier = Modifier
                        .padding(top = topPadding, bottom = bottomPadding),
                )
            }

            composable(NOTIFICATIONS_DESTINATION) {
                NotificationsView(navController = navController)
            }

            composable(ABOUT_DESTINATION) { AboutView(navController = navController) }

            composable(CREDITS_DESTINATION) { CreditsView(navController = navController) }

            composable(MEDIA_DETAILS_DESTINATION,
                arguments = listOf(
                    navArgument("mediaType") { type = NavType.StringType },
                    navArgument("mediaId") { type = NavType.IntType }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://myanimelist.net/{mediaType}/{mediaId}" }
                )
            ) { navEntry ->
                MediaDetailsView(
                    mediaType = navEntry.arguments?.getString("mediaType")
                        ?.let { mediaType -> MediaType.forValue(mediaType) } ?: MediaType.ANIME,
                    mediaId = navEntry.arguments?.getInt("mediaId") ?: 0,
                    navController = navController
                )
            }

            composable(FULL_POSTER_DESTINATION,
                arguments = listOf(
                    navArgument("pictures") { type = stringArrayType }
                )
            ) { navEntry ->
                FullPosterView(
                    pictures = navEntry.arguments?.getStringArray("pictures") ?: emptyArray(),
                    navController = navController
                )
            }

            composable(SETTINGS_DESTINATION) {
                SettingsView(
                    navController = navController
                )
            }

            composable(PROFILE_DESTINATION) {
                ProfileView(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    bottomBarState: MutableState<Boolean>,
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isVisible by remember {
        derivedStateOf {
            when (navBackStackEntry?.destination?.route) {
                HOME_DESTINATION, ANIME_LIST_DESTINATION, MANGA_LIST_DESTINATION, MORE_DESTINATION,
                null -> true
                else -> false
            }
        }
    }
    var query by remember { mutableStateOf("") }
    val performSearch = remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(false) }
    val profilePictureUrl by rememberPreference(PROFILE_PICTURE_PREFERENCE_KEY, "")

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!active) Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                    else Modifier
                )
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchBar(
                query = query,
                onQueryChange = {
                    query = it
                },
                onSearch = {
                    performSearch.value = true
                },
                active = active,
                onActiveChange = {
                    bottomBarState.value = !it
                    active = it
                    if (!active) query = ""
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.search)) },
                leadingIcon = {
                    if (active) {
                        IconButton(
                            onClick = {
                                active = false
                                bottomBarState.value = true
                                query = ""
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "back"
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_search_24),
                            contentDescription = "search"
                        )
                    }
                },
                trailingIcon = {
                    if (!active) {
                        AsyncImage(
                            model = profilePictureUrl,
                            contentDescription = "profile",
                            placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                            error = painterResource(R.drawable.ic_round_account_circle_24),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(100))
                                .size(32.dp)
                                .clickable { navController.navigate(PROFILE_DESTINATION) }
                        )
                    } else if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "delete"
                            )
                        }
                    }
                }
            ) {
                SearchView(
                    query = query,
                    performSearch = performSearch,
                    navController = navController
                )
            }//:SearchBar
        }//:Column
    }
}

private val bottomDestinations = listOf(
    BottomDestination.Home,
    BottomDestination.AnimeList,
    BottomDestination.MangaList,
    BottomDestination.More
)

@Composable
fun BottomNavBar(
    navController: NavController,
    bottomBarState: State<Boolean>,
    lastTabOpened: Int
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isVisible by remember {
        derivedStateOf {
            when (navBackStackEntry?.destination?.route) {
                HOME_DESTINATION, ANIME_LIST_DESTINATION, MANGA_LIST_DESTINATION, MORE_DESTINATION,
                null -> bottomBarState.value
                else -> false
            }
        }
    }
    var selectedItem by rememberPreference(LAST_TAB_PREFERENCE_KEY, lastTabOpened)
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar {
            bottomDestinations.forEachIndexed { index, dest ->
                NavigationBarItem(
                    icon = { Icon(
                        painter = painterResource(if (selectedItem == index) dest.iconSelected else dest.icon),
                        contentDescription = stringResource(dest.title))
                    },
                    label = { Text(text = stringResource(dest.title)) },
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MoeListTheme {
        MainView(
            navController = rememberNavController(),
            lastTabOpened = 0
        )
    }
}