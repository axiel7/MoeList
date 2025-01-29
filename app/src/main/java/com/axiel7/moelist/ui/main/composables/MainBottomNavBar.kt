package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.BottomDestination.Companion.Icon
import com.axiel7.moelist.ui.base.navigation.Route
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainBottomNavBar(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?,
    isVisible: Boolean,
    onItemSelected: (Int) -> Unit,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) {
    val scope = rememberCoroutineScope()

    AnimatedContent(
        targetState = isVisible,
        transitionSpec = {
            slideInVertically(initialOffsetY = { it }) togetherWith
            slideOutVertically(targetOffsetY = { it })
        }
    ) { isVisible ->
        if (isVisible) {
            NavigationBar {
                BottomDestination.values.forEachIndexed { index, dest ->
                    val isSelected = navBackStackEntry?.destination?.hierarchy?.any {
                        it.hasRoute(dest.route::class)
                    } == true
                    NavigationBarItem(
                        icon = { dest.Icon(selected = isSelected) },
                        label = { Text(text = stringResource(dest.title)) },
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                when (dest) {
                                    BottomDestination.More -> {
                                        navController.navigate(Route.Settings)
                                    }

                                    else -> {
                                        navController.navigate(Route.Search(
                                            mediaType = MediaType.MANGA
                                                .takeIf { dest == BottomDestination.MangaList }
                                                ?: MediaType.ANIME
                                        ))
                                    }
                                }
                            } else {
                                scope.launch {
                                    topBarOffsetY.animateTo(0f)
                                }

                                onItemSelected(index)
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}