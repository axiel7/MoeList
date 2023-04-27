package com.axiel7.moelist.uicompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.uicompose.base.BottomDestination
import com.axiel7.moelist.uicompose.base.StringArrayNavType
import com.axiel7.moelist.uicompose.calendar.CALENDAR_DESTINATION
import com.axiel7.moelist.uicompose.calendar.CalendarView
import com.axiel7.moelist.uicompose.details.FULL_POSTER_DESTINATION
import com.axiel7.moelist.uicompose.details.FullPosterView
import com.axiel7.moelist.uicompose.details.MEDIA_DETAILS_DESTINATION
import com.axiel7.moelist.uicompose.details.MediaDetailsView
import com.axiel7.moelist.uicompose.home.HOME_DESTINATION
import com.axiel7.moelist.uicompose.home.HomeView
import com.axiel7.moelist.uicompose.home.HomeViewModel
import com.axiel7.moelist.uicompose.login.LoginActivity
import com.axiel7.moelist.uicompose.more.ABOUT_DESTINATION
import com.axiel7.moelist.uicompose.more.AboutView
import com.axiel7.moelist.uicompose.more.CREDITS_DESTINATION
import com.axiel7.moelist.uicompose.more.CreditsView
import com.axiel7.moelist.uicompose.more.MORE_DESTINATION
import com.axiel7.moelist.uicompose.more.MoreView
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
import com.axiel7.moelist.utils.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.utils.PreferencesDataStore.ANIME_LIST_SORT_PREFERENCE
import com.axiel7.moelist.utils.PreferencesDataStore.LAST_TAB_PREFERENCE_KEY
import com.axiel7.moelist.utils.PreferencesDataStore.MANGA_LIST_SORT_PREFERENCE
import com.axiel7.moelist.utils.PreferencesDataStore.NSFW_PREFERENCE_KEY
import com.axiel7.moelist.utils.PreferencesDataStore.PROFILE_PICTURE_PREFERENCE_KEY
import com.axiel7.moelist.utils.PreferencesDataStore.THEME_PREFERENCE_KEY
import com.axiel7.moelist.utils.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.utils.PreferencesDataStore.getValueSync
import com.axiel7.moelist.utils.PreferencesDataStore.rememberPreference

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val token = defaultPreferencesDataStore.getValueSync(ACCESS_TOKEN_PREFERENCE_KEY)
        if (token != null) App.createKtorClient(token)
        else {
            Intent(this, LoginActivity::class.java).apply { startActivity(this) }
            finish()
        }
        val lastTabOpened = defaultPreferencesDataStore.getValueSync(LAST_TAB_PREFERENCE_KEY)
        defaultPreferencesDataStore.getValueSync(NSFW_PREFERENCE_KEY)?.let {
            App.nsfw = it.toInt()
        }
        val theme = defaultPreferencesDataStore.getValueSync(THEME_PREFERENCE_KEY) ?: "follow_system"
        defaultPreferencesDataStore.getValueSync(ANIME_LIST_SORT_PREFERENCE)?.let {
            App.animeListSort = MediaSort.forValue(it)
        }
        defaultPreferencesDataStore.getValueSync(MANGA_LIST_SORT_PREFERENCE)?.let {
            App.mangaListSort = MediaSort.forValue(it)
        }

        setContent {
            val themePreference by rememberPreference(THEME_PREFERENCE_KEY, theme)

            MoeListTheme(
                darkTheme = if (themePreference == "follow_system") isSystemInDarkTheme() else themePreference == "dark"
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(
                        lastTabOpened = lastTabOpened ?: 0
                    )
                }
            }
        }
    }
}

@Composable
fun MainView(
    lastTabOpened: Int
) {
    val navController = rememberNavController()
    val bottomBarState = remember { mutableStateOf(true) }
    val stringArrayType = remember { StringArrayNavType() }

    val homeViewModel: HomeViewModel = viewModel()

    com.google.accompanist.insets.ui.Scaffold(
        topBar = {
            MainTopAppBar(
                bottomBarState = bottomBarState,
                navController = navController
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
    ) {
        NavHost(
            navController = navController,
            startDestination = when (lastTabOpened) {
                1 -> ANIME_LIST_DESTINATION
                2 -> MANGA_LIST_DESTINATION
                3 -> MORE_DESTINATION
                else -> HOME_DESTINATION
            },
            modifier = Modifier
                .padding(it)
                //.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        ) {
            composable(HOME_DESTINATION) {
                HomeView(
                    viewModel = homeViewModel,
                    navController = navController
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
                    navController = navController
                )
            }

            composable(MANGA_LIST_DESTINATION) {
                UserMediaListHostView(
                    mediaType = MediaType.MANGA,
                    navController = navController
                )
            }

            composable(MORE_DESTINATION) {
                MoreView(
                    navController = navController
                )
            }

            composable(ABOUT_DESTINATION) { AboutView(navController = navController) }

            composable(CREDITS_DESTINATION) { CreditsView(navController = navController) }

            composable(MEDIA_DETAILS_DESTINATION,
                arguments = listOf(
                    navArgument("mediaType") { type = NavType.StringType },
                    navArgument("mediaId") { type = NavType.IntType }
                )
            ) { navEntry ->
                MediaDetailsView(
                    mediaType = MediaType.valueOf(navEntry.arguments?.getString("mediaType") ?: "ANIME"),
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
    navController: NavController
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
            modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
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
                            Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "delete")
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
        }
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
                    icon = { Icon(painter = painterResource(dest.icon), contentDescription = stringResource(dest.title)) },
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
            lastTabOpened = 0
        )
    }
}