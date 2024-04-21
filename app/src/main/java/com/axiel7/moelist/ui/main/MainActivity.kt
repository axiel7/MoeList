package com.axiel7.moelist.ui.main

import android.content.Intent
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.BottomDestination.Companion.toBottomDestinationIndex
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.NavActionManager.Companion.rememberNavActionManager
import com.axiel7.moelist.ui.main.composables.MainBottomNavBar
import com.axiel7.moelist.ui.main.composables.MainNavigationRail
import com.axiel7.moelist.ui.main.composables.MainTopAppBar
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.dark_scrim
import com.axiel7.moelist.ui.theme.light_scrim
import com.axiel7.moelist.utils.MOELIST_PAGELINK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, KoinExperimentalAPI::class)
class MainActivity : AppCompatActivity() {

    val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        checkLoginIntent(intent)

        App.accessToken = runBlocking { viewModel.accessToken.first() }

        runBlocking {
            App.titleLanguage = viewModel.titleLanguage.first()
        }

        val mediaIdAndType = findMediaIdAndTypeFromIntent()
        val mediaId = mediaIdAndType?.first
        val mediaTypeArg = mediaIdAndType?.second

        val lastTabOpened = findLastTabOpened()
        val initialTheme = runBlocking { viewModel.theme.first() }
        val initialUseBlackColors = runBlocking { viewModel.useBlackColors.first() }

        setContent {
            KoinAndroidContext {
                val theme by viewModel.theme.collectAsStateWithLifecycle(initialValue = initialTheme)
                val useBlackColors by viewModel.useBlackColors.collectAsStateWithLifecycle(
                    initialValue = initialUseBlackColors
                )
                val isDark = if (theme == ThemeStyle.FOLLOW_SYSTEM) isSystemInDarkTheme()
                else theme == ThemeStyle.DARK

                val navController = rememberNavController()
                val navActionManager = rememberNavActionManager(navController)
                val navBackStackEntry by navController.currentBackStackEntryAsState()

                val windowSizeClass = calculateWindowSizeClass(this)
                val isCompactScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

                val accessToken by viewModel.accessToken.collectAsStateWithLifecycle(App.accessToken)
                val useListTabs by viewModel.useListTabs.collectAsStateWithLifecycle()
                val profilePicture by viewModel.profilePicture.collectAsStateWithLifecycle()

                MoeListTheme(
                    darkTheme = isDark,
                    useBlackColors = useBlackColors
                ) {
                    val backgroundColor = MaterialTheme.colorScheme.background
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = backgroundColor
                    ) {
                        MainView(
                            isCompactScreen = isCompactScreen,
                            isLoggedIn = !accessToken.isNullOrEmpty(),
                            useListTabs = useListTabs,
                            navController = navController,
                            navActionManager = navActionManager,
                            lastTabOpened = lastTabOpened,
                            saveLastTab = viewModel::saveLastTab,
                            profilePicture = profilePicture,
                        )

                        DisposableEffect(isDark, navBackStackEntry) {
                            var statusBarStyle = SystemBarStyle.auto(
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT
                            ) { isDark }

                            if (isCompactScreen
                                && BottomDestination.routes.contains(navBackStackEntry?.destination?.route)
                            ) {
                                statusBarStyle =
                                    if (isDark) SystemBarStyle.dark(backgroundColor.toArgb())
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
                                ) { isDark },
                            )
                            onDispose {}
                        }
                    }
                }

                LaunchedEffect(mediaId) {
                    if (mediaId != null && mediaTypeArg != null) {
                        val mediaType = MediaType.valueOf(mediaTypeArg)
                        navActionManager.toMediaDetails(mediaType, mediaId)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkLoginIntent(intent)
    }

    private fun checkLoginIntent(intent: Intent?) {
        if (intent?.data?.toString()?.startsWith(MOELIST_PAGELINK) == true) {
            intent.data?.let { viewModel.parseIntentData(it) }
        }
    }

    private fun findLastTabOpened(): Int {
        val startTab = runBlocking { viewModel.startTab.first() }
        var lastTabOpened =
            intent.action?.toBottomDestinationIndex() ?: startTab?.value?.toBottomDestinationIndex()

        if (lastTabOpened == null) {
            lastTabOpened = runBlocking { viewModel.lastTab.first() }
        } else { // opened from intent or start tab setting
            viewModel.saveLastTab(lastTabOpened)
        }
        return lastTabOpened
    }

    private fun findMediaIdAndTypeFromIntent(): Pair<Int, String>? {
        if (intent.action == "details") {
            val mediaId = intent.getIntExtra("media_id", 0)
            val mediaType = intent.getStringExtra("media_type")?.uppercase()
            if (mediaId != 0 && mediaType != null) return mediaId to mediaType
        } else if (intent.data != null) {
            // Manually handle deep links because the uri pattern in the compose navigation
            // matches this -> https://myanimelist.net/manga/11514
            // but not this -> https://myanimelist.net/manga/11514/Otoyomegatari
            //TODO: find a better solution :)
            val malSchemeIndex = intent.dataString?.indexOf("myanimelist.net")
            if (malSchemeIndex != null && malSchemeIndex != -1) {
                val linkSplit = intent.dataString?.substring(malSchemeIndex)?.split('/').orEmpty()
                val mediaType = linkSplit.getOrNull(1)?.uppercase()
                val mediaId = linkSplit.getOrNull(2)?.toIntOrNull()
                if (mediaType != null && mediaId != null) return mediaId to mediaType
            }
        }
        return null
    }
}

@Composable
fun MainView(
    isCompactScreen: Boolean,
    isLoggedIn: Boolean,
    useListTabs: Boolean,
    navController: NavHostController,
    navActionManager: NavActionManager,
    lastTabOpened: Int,
    saveLastTab: (Int) -> Unit,
    profilePicture: String?,
) {
    val density = LocalDensity.current

    val bottomBarState = remember { mutableStateOf(true) }
    var topBarHeightPx by remember { mutableFloatStateOf(0f) }
    val topBarOffsetY = remember { Animatable(0f) }

    Scaffold(
        topBar = {
            if (isCompactScreen) {
                MainTopAppBar(
                    profilePicture = profilePicture,
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
                    onItemSelected = saveLastTab,
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
                    onItemSelected = saveLastTab,
                )
                MainNavigation(
                    navController = navController,
                    navActionManager = navActionManager,
                    lastTabOpened = lastTabOpened,
                    isLoggedIn = isLoggedIn,
                    isCompactScreen = false,
                    useListTabs = useListTabs,
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
                navActionManager = navActionManager,
                lastTabOpened = lastTabOpened,
                isLoggedIn = isLoggedIn,
                isCompactScreen = true,
                useListTabs = useListTabs,
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

@Preview
@Composable
fun MainPreview() {
    MoeListTheme {
        Surface {
            MainView(
                isCompactScreen = true,
                isLoggedIn = false,
                useListTabs = false,
                navController = rememberNavController(),
                navActionManager = rememberNavActionManager(),
                lastTabOpened = 0,
                saveLastTab = {},
                profilePicture = null,
            )
        }
    }
}
