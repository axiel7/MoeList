package com.axiel7.moelist.uicompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.home.HOME_DESTINATION
import com.axiel7.moelist.uicompose.home.HomeView
import com.axiel7.moelist.uicompose.home.HomeViewModel
import com.axiel7.moelist.uicompose.theme.MoeListTheme

//Destination constants
const val ANIME_LIST_DESTINATION = "anime_list"
const val MANGA_LIST_DESTINATION = "manga_list"
const val MORE_DESTINATION = "more"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = { MainTopAppBar(topAppBarScrollBehavior) },
        bottomBar = { BottomNavBar(navController) }
    ) {
        NavHost(
            navController = navController,
            startDestination = HOME_DESTINATION,
            modifier = Modifier.padding(it)
        ) {
            composable(HOME_DESTINATION) {
                HomeView(
                    viewModel = homeViewModel,
                    navController = navController
                )
            }
            composable(ANIME_LIST_DESTINATION) { }
            composable(MANGA_LIST_DESTINATION) { }
            composable(MORE_DESTINATION) { }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(scrollBehavior: TopAppBarScrollBehavior) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                    .clickable { }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_search_24),
                        contentDescription = "search",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Search",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }

                AsyncImage(
                    model = "",
                    contentDescription = "profile",
                    placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                    error = painterResource(R.drawable.ic_round_account_circle_24),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(100))
                        .size(32.dp)
                        .clickable { }
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun BottomNavBar(navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
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

@Preview(showBackground = true, showSystemUi = true, device = Devices.DEFAULT)
@Composable
fun MainPreview() {
    MoeListTheme {
        MainView()
    }
}