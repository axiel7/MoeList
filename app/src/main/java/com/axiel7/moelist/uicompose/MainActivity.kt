package com.axiel7.moelist.uicompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.listStatusAnimeValues
import com.axiel7.moelist.data.model.media.listStatusMangaValues
import com.axiel7.moelist.uicompose.base.TabRowItem
import com.axiel7.moelist.uicompose.details.MEDIA_DETAILS_DESTINATION
import com.axiel7.moelist.uicompose.details.MediaDetailsView
import com.axiel7.moelist.uicompose.home.HOME_DESTINATION
import com.axiel7.moelist.uicompose.home.HomeView
import com.axiel7.moelist.uicompose.home.HomeViewModel
import com.axiel7.moelist.uicompose.login.LoginActivity
import com.axiel7.moelist.uicompose.more.MORE_DESTINATION
import com.axiel7.moelist.uicompose.more.MoreView
import com.axiel7.moelist.uicompose.more.SETTINGS_DESTINATION
import com.axiel7.moelist.uicompose.more.SettingsView
import com.axiel7.moelist.uicompose.profile.PROFILE_DESTINATION
import com.axiel7.moelist.uicompose.profile.ProfileView
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.uicompose.userlist.ANIME_LIST_DESTINATION
import com.axiel7.moelist.uicompose.userlist.MANGA_LIST_DESTINATION
import com.axiel7.moelist.uicompose.userlist.UserMediaListHostView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (!App.isUserLogged || App.accessToken == null || App.accessToken == "null") {
            Intent(this, LoginActivity::class.java).apply { startActivity(this) }
            finish()
            return
        }

        setContent {
            MoeListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }
}

@Composable
fun MainView() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
    val topBarState = rememberSaveable { (mutableStateOf(true)) }

    val homeViewModel: HomeViewModel = viewModel()
    val animeTabs = remember {
        listStatusAnimeValues().map { TabRowItem(
            value = it,
            title = it.value
        ) }
    }
    val mangaTabs = remember {
        listStatusMangaValues().map { TabRowItem(
            value = it,
            title = it.value
        ) }
    }

    com.google.accompanist.insets.ui.Scaffold(
        topBar = {
            MainTopAppBar(
                topBarState = topBarState,
                navController = navController
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                bottomBarState = bottomBarState
            )
        },
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = ANIME_LIST_DESTINATION,
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
            composable(ANIME_LIST_DESTINATION) {
                UserMediaListHostView(
                    mediaType = MediaType.ANIME,
                    tabRowItems = animeTabs,
                    navController = navController
                )
            }
            composable(MANGA_LIST_DESTINATION) {
                UserMediaListHostView(
                    mediaType = MediaType.MANGA,
                    tabRowItems = mangaTabs,
                    navController = navController
                )
            }
            composable(MORE_DESTINATION) {
                MoreView(
                    navController = navController
                )
            }

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

    LaunchedEffect(navBackStackEntry) {
        snapshotFlow { navBackStackEntry?.destination }.collect {
            when (it?.route) {
                MEDIA_DETAILS_DESTINATION, SETTINGS_DESTINATION, PROFILE_DESTINATION -> {
                    topBarState.value = false
                    bottomBarState.value = false
                }
                else -> {
                    topBarState.value = true
                    bottomBarState.value = true
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    topBarState: State<Boolean>,
    navController: NavController
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = topBarState.value,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(text = "Search") },
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = { active = false }) {
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
                            model = "",
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
                Text(text = "Search view")
            }//:SearchBar
        }
    }
}

@Composable
fun BottomNavBar(
    navController: NavController,
    bottomBarState: State<Boolean>
) {
    var selectedItem by remember { mutableStateOf(1) }
    
    AnimatedVisibility(
        visible = bottomBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_outline_home_24), contentDescription = stringResource(id = R.string.title_home)) },
                label = { Text(stringResource(id = R.string.title_home)) },
                selected = selectedItem == 0,
                onClick = {
                    selectedItem = 0
                    navController.navigate(HOME_DESTINATION)
                }
            )

            NavigationBarItem(
                icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_outline_local_movies_24), contentDescription = stringResource(id = R.string.title_anime_list)) },
                label = { Text(stringResource(id = R.string.title_anime_list)) },
                selected = selectedItem == 1,
                onClick = {
                    selectedItem = 1
                    navController.navigate(ANIME_LIST_DESTINATION)
                }
            )

            NavigationBarItem(
                icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_outline_book_24), contentDescription = stringResource(id = R.string.title_manga_list)) },
                label = { Text(stringResource(id = R.string.title_manga_list)) },
                selected = selectedItem == 2,
                onClick = {
                    selectedItem = 2
                    navController.navigate(MANGA_LIST_DESTINATION)
                }
            )

            NavigationBarItem(
                icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_more_horizontal), contentDescription = stringResource(id = R.string.more)) },
                label = { Text(stringResource(id = R.string.more)) },
                selected = selectedItem == 3,
                onClick = {
                    selectedItem = 3
                    navController.navigate(MORE_DESTINATION)
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.DEFAULT)
@Composable
fun MainPreview() {
    MoeListTheme {
        MainView()
    }
}