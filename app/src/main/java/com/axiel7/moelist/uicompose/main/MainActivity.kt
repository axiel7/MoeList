package com.axiel7.moelist.uicompose.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.App
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
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LOAD_CHARACTERS_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_COMPLETED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_CURRENT_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_DROPPED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_LIST_SORT_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_PAUSED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.MANGA_PLANNED_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.NSFW_PREFERENCE_KEY
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
import com.axiel7.moelist.uicompose.details.MEDIA_DETAILS_DESTINATION
import com.axiel7.moelist.uicompose.main.composables.MainBottomNavBar
import com.axiel7.moelist.uicompose.main.composables.MainNavigationRail
import com.axiel7.moelist.uicompose.main.composables.MainTopAppBar
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.uicompose.theme.dark_scrim
import com.axiel7.moelist.uicompose.theme.light_scrim
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.NumExtensions.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // login intent
        if (intent.data?.toString()?.startsWith(Constants.MOELIST_PAGELINK) == true) {
            intent.data?.let { parseIntentData(it) }
        }

        preloadPreferences()

        val startTab = defaultPreferencesDataStore.getValueSync(START_TAB_PREFERENCE_KEY)
        var lastTabOpened =
            intent.action?.toBottomDestinationIndex() ?: startTab?.toBottomDestinationIndex()
        var mediaId: Int? = null
        var mediaType: String? = null
        if (intent.action == "details") {
            mediaId = intent.getIntExtra("media_id", 0)
            mediaType = intent.getStringExtra("media_type")?.uppercase()
        } else if (intent.data != null) {
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
        } else { // opened from intent or start tab setting
            CoroutineScope(Dispatchers.IO).launch {
                defaultPreferencesDataStore.edit {
                    it[LAST_TAB_PREFERENCE_KEY] = lastTabOpened
                }
            }
        }

        val theme =
            defaultPreferencesDataStore.getValueSync(THEME_PREFERENCE_KEY) ?: "follow_system"

        setContent {
            val themePreference by rememberPreference(THEME_PREFERENCE_KEY, theme)
            val darkTheme = if (themePreference == "follow_system") isSystemInDarkTheme()
            else themePreference == "dark" || themePreference == "black"

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            val windowSizeClass = calculateWindowSizeClass(this)
            val isCompactScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

            MoeListTheme(
                darkTheme = darkTheme,
                amoledColors = themePreference == "black"
            ) {
                val backgroundColor = MaterialTheme.colorScheme.background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    MainView(
                        isCompactScreen = isCompactScreen,
                        navController = navController,
                        lastTabOpened = lastTabOpened ?: 0
                    )

                    DisposableEffect(darkTheme, navBackStackEntry) {
                        var statusBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        ) { darkTheme }

                        if (isCompactScreen
                            && BottomDestination.routes.contains(navBackStackEntry?.destination?.route)
                        ) {
                            statusBarStyle =
                                if (darkTheme) SystemBarStyle.dark(backgroundColor.toArgb())
                                else SystemBarStyle.light(
                                    backgroundColor.toArgb(),
                                    dark_scrim.toArgb()
                                )
                        }
                        enableEdgeToEdge(
                            statusBarStyle = statusBarStyle,
                            navigationBarStyle = SystemBarStyle.auto(
                                light_scrim.toArgb(),
                                dark_scrim.toArgb(),
                            ) { darkTheme },
                        )
                        onDispose {}
                    }
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
        defaultPreferencesDataStore.getValueSync(LOAD_CHARACTERS_PREFERENCE_KEY)?.let {
            App.loadCharacters = it
        }

        // load preferences used later in another thread
        lifecycleScope.launch(Dispatchers.IO) {
            defaultPreferencesDataStore.getValueSync(ANIME_PLANNED_LIST_STYLE_PREFERENCE_KEY)?.let {
                ListStyle.forValue(it)?.let { mode -> App.animePlannedListStyle = mode }
            }
            defaultPreferencesDataStore.getValueSync(ANIME_COMPLETED_LIST_STYLE_PREFERENCE_KEY)
                ?.let {
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
            defaultPreferencesDataStore.getValueSync(MANGA_COMPLETED_LIST_STYLE_PREFERENCE_KEY)
                ?.let {
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
    isCompactScreen: Boolean,
    navController: NavHostController,
    lastTabOpened: Int
) {
    val density = LocalDensity.current

    val bottomBarState = remember { mutableStateOf(true) }
    var topBarHeightPx by remember { mutableFloatStateOf(0f) }
    val topBarOffsetY = remember { Animatable(0f) }

    Scaffold(
        topBar = {
            if (isCompactScreen) {
                MainTopAppBar(
                    bottomBarState = bottomBarState,
                    navController = navController,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = topBarOffsetY.value
                        }
                )
            }
        },
        bottomBar = {
            if (isCompactScreen) {
                MainBottomNavBar(
                    navController = navController,
                    bottomBarState = bottomBarState,
                    lastTabOpened = lastTabOpened,
                    topBarOffsetY = topBarOffsetY,
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        if (!isCompactScreen) {
            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
            Row(
                modifier = Modifier.padding(padding)
            ) {
                MainNavigationRail(
                    navController = navController,
                    lastTabOpened = lastTabOpened,
                )
                MainNavigation(
                    navController = navController,
                    lastTabOpened = lastTabOpened,
                    isCompactScreen = false,
                    modifier = Modifier,
                    padding = PaddingValues(
                        start = padding.calculateStartPadding(LocalLayoutDirection.current),
                        top = systemBarsPadding.calculateTopPadding(),
                        end = padding.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = systemBarsPadding.calculateBottomPadding()
                    ),
                    topBarHeightPx = topBarHeightPx,
                    topBarOffsetY = topBarOffsetY,
                )
            }
        } else {
            LaunchedEffect(padding) {
                topBarHeightPx = density.run { padding.calculateTopPadding().toPx() }
            }
            MainNavigation(
                navController = navController,
                lastTabOpened = lastTabOpened,
                isCompactScreen = true,
                modifier = Modifier.padding(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                ),
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MoeListTheme {
        MainView(
            isCompactScreen = true,
            navController = rememberNavController(),
            lastTabOpened = 0
        )
    }
}
