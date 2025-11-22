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
import androidx.navigation3.runtime.NavKey
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.BottomDestination.Companion.Icon
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.TopLevelBackStack
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainBottomNavBar(
    topLevelBackStack: TopLevelBackStack<NavKey>,
    navActionManager: NavActionManager,
    isVisible: Boolean,
    onItemSelected: (Int) -> Unit,
    showTopBar: () -> Unit,
) {
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
                    val isSelected = dest.route == topLevelBackStack.topLevelKey
                    NavigationBarItem(
                        icon = { dest.Icon(selected = isSelected) },
                        label = { Text(text = stringResource(dest.title)) },
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                when (dest) {
                                    BottomDestination.More -> {
                                        navActionManager.toSettings()
                                    }

                                    else -> {
                                        navActionManager.toSearch(
                                            mediaType = MediaType.MANGA
                                                .takeIf { dest == BottomDestination.MangaList }
                                                ?: MediaType.ANIME
                                        )
                                    }
                                }
                            } else {
                                showTopBar()

                                onItemSelected(index)
                                topLevelBackStack.addTopLevel(dest.route)
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