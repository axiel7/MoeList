package com.axiel7.moelist.uicompose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_COMPLETED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_CURRENT_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_DROPPED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_LIST_SORT_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_PAUSED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ANIME_PLANNED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GRID_ITEMS_PER_ROW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LAST_TAB_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_COMPLETED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_CURRENT_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_DROPPED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_LIST_SORT_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_PAUSED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_PLANNED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.NSFW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.PROFILE_PICTURE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.START_TAB_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.THEME_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.TITLE_LANG_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_LIST_TABS_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.data.datastore.PreferencesDataStore.getValueSync
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.LoginRepository
import com.axiel7.moelist.uicompose.base.BottomDestination
import com.axiel7.moelist.uicompose.base.BottomDestination.Companion.toBottomDestinationIndex
import com.axiel7.moelist.uicompose.base.ListStyle
import com.axiel7.moelist.uicompose.composables.BackIconButton
import com.axiel7.moelist.uicompose.details.MEDIA_DETAILS_DESTINATION
import com.axiel7.moelist.uicompose.home.HOME_DESTINATION
import com.axiel7.moelist.uicompose.more.MORE_DESTINATION
import com.axiel7.moelist.uicompose.profile.PROFILE_DESTINATION
import com.axiel7.moelist.uicompose.search.SearchView
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.uicompose.userlist.ANIME_LIST_DESTINATION
import com.axiel7.moelist.uicompose.userlist.MANGA_LIST_DESTINATION
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.NumExtensions.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // login intent
        if (intent.data?.toString()?.startsWith(Constants.MOELIST_PAGELINK) == true) {
            intent.data?.let { parseIntentData(it) }
        }

        preloadPreferences()

        val startTab = defaultPreferencesDataStore.getValueSync(START_TAB_PREFERENCE_KEY)
        var lastTabOpened = intent.action?.toBottomDestinationIndex() ?: startTab?.toBottomDestinationIndex()
        var mediaId: Int? = null
        var mediaType: String? = null
        if (intent.action == "details") {
            mediaId = intent.getIntExtra("media_id", 0)
            mediaType = intent.getStringExtra("media_type")?.uppercase()
        }
        else if (intent.data != null) {
            // Manually handle deep links because the uri pattern in the compose navigation
            // matches this -> https://myanimelist.net/manga/11514
            // but not this -> https://myanimelist.net/manga/11514/Otoyomegatari
            //TODO: find a better solution :)
            val malSchemeIndex = intent.dataString?.indexOf("myanimelist.net")
            if (malSchemeIndex != null && malSchemeIndex != -1) {
                val linkSplit = intent.dataString!!.substring(malSchemeIndex).split('/')
                mediaType = linkSplit[1].uppercase()
                mediaId = linkSplit[2].toIntOrNull()
            }
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

        val theme = defaultPreferencesDataStore.getValueSync(THEME_PREFERENCE_KEY) ?: "follow_system"

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
                if (mediaId != null && mediaId != 0 && mediaType != null) {
                    navController.navigate(
                        MEDIA_DETAILS_DESTINATION
                            .replace("{mediaType}", mediaType)
                            .replace("{mediaId}", mediaId.toString())
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data?.toString()?.startsWith(Constants.MOELIST_PAGELINK) == true) {
            intent.data?.let { parseIntentData(it) }
        }
    }

    private fun parseIntentData(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val receivedState = uri.getQueryParameter("state")
        if (code != null && receivedState == LoginRepository.STATE) {
            lifecycleScope.launch(Dispatchers.IO) { LoginRepository.getAccessToken(code) }
        }
    }

    private fun preloadPreferences() {
        //Cache DataStore in memory
        lifecycleScope.launch {
            defaultPreferencesDataStore.data.first()
        }

        defaultPreferencesDataStore.getValueSync(ACCESS_TOKEN_PREFERENCE_KEY)?.let {
            App.createKtorClient(accessToken = it)
        }
        defaultPreferencesDataStore.getValueSync(NSFW_PREFERENCE_KEY)?.let {
            App.nsfw = it.toInt()
        }
        defaultPreferencesDataStore.getValueSync(ANIME_LIST_SORT_PREFERENCE_KEY)?.let {
            MediaSort.forValue(it)?.let { sort -> App.animeListSort = sort }
        }
        defaultPreferencesDataStore.getValueSync(MANGA_LIST_SORT_PREFERENCE_KEY)?.let {
            MediaSort.forValue(it)?.let { sort -> App.mangaListSort = sort }
        }

        defaultPreferencesDataStore.getValueSync(GENERAL_LIST_STYLE_PREFERENCE_KEY)?.let {
            ListStyle.forValue(it)?.let { mode -> App.generalListStyle = mode }
        }
        defaultPreferencesDataStore.getValueSync(USE_GENERAL_LIST_STYLE_PREFERENCE_KEY)?.let {
            App.useGeneralListStyle = it
        }
        defaultPreferencesDataStore.getValueSync(ANIME_CURRENT_LIST_STYLE_PREFERENCE_KEY)?.let {
            ListStyle.forValue(it)?.let { mode -> App.animeCurrentListStyle = mode }
        }
        defaultPreferencesDataStore.getValueSync(MANGA_CURRENT_LIST_STYLE_PREFERENCE_KEY)?.let {
            ListStyle.forValue(it)?.let { mode -> App.mangaCurrentListStyle = mode }
        }

        defaultPreferencesDataStore.getValueSync(TITLE_LANG_PREFERENCE_KEY)?.let {
            App.titleLanguage = TitleLanguage.valueOf(it)
        }
        defaultPreferencesDataStore.getValueSync(USE_LIST_TABS_PREFERENCE_KEY)?.let {
            App.useListTabs = it
        }
        defaultPreferencesDataStore.getValueSync(GRID_ITEMS_PER_ROW_PREFERENCE_KEY)?.let {
            App.gridItemsPerRow = it
        }

        // load preferences used later in another thread
        lifecycleScope.launch(Dispatchers.IO) {
            defaultPreferencesDataStore.getValueSync(ANIME_PLANNED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.animePlannedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(ANIME_COMPLETED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.animeCompletedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(ANIME_PAUSED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.animePausedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(ANIME_DROPPED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.animeDroppedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(MANGA_PLANNED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.mangaPlannedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(MANGA_COMPLETED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.mangaCompletedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(MANGA_PAUSED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.mangaPausedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(MANGA_DROPPED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.mangaDroppedListStyle = mode }
            }
        }
    }
}

@Composable
fun MainView(
    navController: NavHostController,
    lastTabOpened: Int
) {
    val density = LocalDensity.current

    val bottomBarState = remember { mutableStateOf(true) }
    var topBarHeightPx by remember { mutableFloatStateOf(0f) }
    val topBarOffsetY = remember { Animatable(0f) }

    Scaffold(
        topBar = {
            MainTopAppBar(
                bottomBarState = bottomBarState,
                navController = navController,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = topBarOffsetY.value
                    }
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                bottomBarState = bottomBarState,
                lastTabOpened = lastTabOpened,
                topBarOffsetY = topBarOffsetY,
            )
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        LaunchedEffect(key1 = padding) {
            topBarHeightPx = density.run { padding.calculateTopPadding().toPx() }
        }

        MainNavigation(
            navController = navController,
            lastTabOpened = lastTabOpened,
            padding = padding,
            topBarHeightPx = topBarHeightPx,
            topBarOffsetY = topBarOffsetY,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    bottomBarState: MutableState<Boolean>,
    navController: NavController,
    modifier: Modifier = Modifier,
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
            modifier = modifier
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
                        BackIconButton(
                            onClick = {
                                active = false
                                bottomBarState.value = true
                                query = ""
                            }
                        )
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
                    navigateToMediaDetails = { mediaType, mediaId ->
                        navController.navigate(
                            MEDIA_DETAILS_DESTINATION
                                .replace("{mediaType}", mediaType.name)
                                .replace("{mediaId}", mediaId.toString())
                        )
                    }
                )
            }//:SearchBar
        }//:Column
    }
}

@Composable
fun BottomNavBar(
    navController: NavController,
    bottomBarState: State<Boolean>,
    lastTabOpened: Int,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) {
    val scope = rememberCoroutineScope()

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
            BottomDestination.values.forEachIndexed { index, dest ->
                NavigationBarItem(
                    icon = { Icon(
                        painter = painterResource(if (selectedItem == index) dest.iconSelected else dest.icon),
                        contentDescription = stringResource(dest.title))
                    },
                    label = { Text(text = stringResource(dest.title)) },
                    selected = selectedItem == index,
                    onClick = {
                        scope.launch {
                            topBarOffsetY.animateTo(0f)
                        }

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
