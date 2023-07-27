package com.axiel7.moelist.uicompose.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.icon
import com.axiel7.moelist.data.model.media.listStatusAnimeValues
import com.axiel7.moelist.data.model.media.listStatusMangaValues
import com.axiel7.moelist.data.model.media.localized
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.preferences.ListPreferenceView

const val LIST_STYLE_SETTINGS_DESTINATION = "list_style_settings"

@Composable
fun ListStyleSettingsView(
    navigateBack: () -> Unit,
) {
    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.list_style),
        navigateBack = navigateBack
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
                val listType = ListType(status, MediaType.ANIME)
                var preference by rememberPreference(
                    listType.stylePreferenceKey,
                    listType.styleGlobalAppVariable.value
                )

                ListPreferenceView(
                    title = status.localized(),
                    entriesValues = listStyleEntries,
                    value = preference,
                    icon = status.icon(),
                    onValueChange = {
                        preference = it
                    }
                )
            }

            SettingsTitle(text = stringResource(R.string.title_manga_list))
            listStatusMangaValues.forEach { status ->
                val listType = ListType(status, MediaType.MANGA)
                var preference by rememberPreference(
                    listType.stylePreferenceKey,
                    listType.styleGlobalAppVariable.value
                )

                ListPreferenceView(
                    title = status.localized(),
                    entriesValues = listStyleEntries,
                    value = preference,
                    icon = status.icon(),
                    onValueChange = {
                        preference = it
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun ListStyleSettingsViewPreview() {
    ListStyleSettingsView(
        navigateBack = {}
    )
}