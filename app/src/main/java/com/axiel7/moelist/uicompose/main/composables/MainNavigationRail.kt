package com.axiel7.moelist.uicompose.main.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore
import com.axiel7.moelist.uicompose.base.BottomDestination
import com.axiel7.moelist.uicompose.base.BottomDestination.Companion.Icon
import com.axiel7.moelist.uicompose.search.SEARCH_DESTINATION

@Composable
fun MainNavigationRail(
    navController: NavController,
    lastTabOpened: Int,
) {
    var selectedItem by PreferencesDataStore.rememberPreference(
        PreferencesDataStore.LAST_TAB_PREFERENCE_KEY,
        lastTabOpened
    )

    NavigationRail(
        header = {
            FloatingActionButton(
                onClick = {
                    selectedItem = -1
                    navController.navigate(SEARCH_DESTINATION) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_search_24),
                    contentDescription = stringResource(R.string.search)
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.weight(1f))
        BottomDestination.railValues.forEachIndexed { index, dest ->
            NavigationRailItem(
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
                },
                icon = { dest.Icon(selected = selectedItem == index) },
                label = { Text(text = stringResource(dest.title)) }
            )
        }
    }
}