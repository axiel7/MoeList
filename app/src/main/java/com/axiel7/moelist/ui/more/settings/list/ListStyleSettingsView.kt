package com.axiel7.moelist.ui.more.settings.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusAnimeValues
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusMangaValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.preferences.ListPreferenceView
import com.axiel7.moelist.ui.more.settings.SettingsTitle
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ListStyleSettingsView(
    navActionManager: NavActionManager
) {
    val viewModel: ListStyleSettingsViewModel = koinViewModel()

    ListStyleSettingsViewContent(
        event = viewModel,
        navActionManager = navActionManager
    )
}

@Composable
private fun ListStyleSettingsViewContent(
    event: ListStyleSettingsEvent?,
    navActionManager: NavActionManager
) {
    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.list_style),
        navigateBack = navActionManager::goBack
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Text(
                text = stringResource(R.string.changes_will_take_effect_on_app_restart),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SettingsTitle(text = stringResource(R.string.title_anime_list))
            listStatusAnimeValues.forEach { status ->
                val style = event?.getListStyle(MediaType.ANIME, status)
                    ?.collectAsStateWithLifecycle()

                ListPreferenceView(
                    title = status.localized(),
                    entriesValues = ListStyle.entriesLocalized,
                    value = style?.value ?: ListStyle.STANDARD,
                    icon = status.icon,
                    onValueChange = {
                        event?.setListStyle(MediaType.ANIME, status, it)
                    }
                )
            }

            SettingsTitle(text = stringResource(R.string.title_manga_list))
            listStatusMangaValues.forEach { status ->
                val style = event?.getListStyle(MediaType.MANGA, status)
                    ?.collectAsStateWithLifecycle()

                ListPreferenceView(
                    title = status.localized(),
                    entriesValues = ListStyle.entriesLocalized,
                    value = style?.value ?: ListStyle.STANDARD,
                    icon = status.icon,
                    onValueChange = {
                        event?.setListStyle(MediaType.MANGA, status, it)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun ListStyleSettingsViewPreview() {
    MoeListTheme {
        Surface {
            ListStyleSettingsViewContent(
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}