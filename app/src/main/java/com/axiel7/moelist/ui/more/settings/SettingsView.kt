package com.axiel7.moelist.ui.more.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.AppLanguage
import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.preferences.ListPreferenceView
import com.axiel7.moelist.ui.composables.preferences.PlainPreferenceView
import com.axiel7.moelist.ui.composables.preferences.SwitchPreferenceView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings
import org.koin.androidx.compose.navigation.koinNavViewModel

@Composable
fun SettingsView(
    navActionManager: NavActionManager
) {
    val viewModel: SettingsViewModel = koinNavViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@Composable
private fun SettingsViewContent(
    uiState: SettingsUiState,
    event: SettingsEvent?,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.settings),
        navigateBack = navActionManager::goBack
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            SettingsTitle(text = stringResource(R.string.display))

            ListPreferenceView(
                title = stringResource(R.string.theme),
                entriesValues = ThemeStyle.entriesLocalized,
                value = uiState.theme,
                icon = R.drawable.ic_round_color_lens_24,
                onValueChange = { event?.setTheme(it) }
            )

            SwitchPreferenceView(
                title = stringResource(R.string.black_theme_variant),
                value = uiState.useBlackColors,
                onValueChange = { event?.setUseBlackColors(it) }
            )

            ListPreferenceView(
                title = stringResource(R.string.language),
                entriesValues = AppLanguage.entriesLocalized,
                value = uiState.language,
                icon = R.drawable.ic_round_language_24,
                onValueChange = { event?.setLanguage(it) }
            )

            ListPreferenceView(
                title = stringResource(R.string.title_language),
                entriesValues = TitleLanguage.entriesLocalized,
                value = uiState.titleLanguage,
                icon = R.drawable.round_title_24,
                onValueChange = { event?.setTitleLanguage(it) }
            )

            ListPreferenceView(
                title = stringResource(R.string.default_section),
                entriesValues = StartTab.entriesLocalized,
                value = uiState.startTab,
                icon = R.drawable.ic_round_home_24,
                onValueChange = { event?.setStartTab(it) }
            )

            SwitchPreferenceView(
                title = stringResource(R.string.use_separated_list_styles),
                value = !uiState.useGeneralListStyle,
                onValueChange = { event?.setUseGeneralListStyle(!it) }
            )

            if (uiState.useGeneralListStyle) {
                ListPreferenceView(
                    title = stringResource(R.string.list_style),
                    entriesValues = ListStyle.entriesLocalized,
                    value = uiState.generalListStyle,
                    icon = R.drawable.round_format_list_bulleted_24,
                    onValueChange = { event?.setGeneralListStyle(it) }
                )
            } else {
                PlainPreferenceView(
                    title = stringResource(R.string.list_style),
                    icon = R.drawable.round_format_list_bulleted_24,
                    onClick = navActionManager::toListStyleSettings
                )
            }

            if (uiState.generalListStyle == ListStyle.GRID || !uiState.useGeneralListStyle) {
                ListPreferenceView(
                    title = stringResource(R.string.items_per_row),
                    entriesValues = ItemsPerRow.entriesLocalized,
                    value = uiState.itemsPerRow,
                    icon = R.drawable.round_grid_view_24,
                    onValueChange = { event?.setItemsPerRow(it) }
                )
            }

            SettingsTitle(text = stringResource(R.string.content))

            SwitchPreferenceView(
                title = stringResource(R.string.show_nsfw),
                subtitle = stringResource(R.string.nsfw_summary),
                value = uiState.showNsfw,
                icon = R.drawable.no_adult_content_24,
                onValueChange = { event?.setShowNsfw(it) }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PlainPreferenceView(
                    title = stringResource(R.string.open_mal_links_in_the_app),
                    icon = R.drawable.ic_open_in_browser,
                    onClick = {
                        context.openByDefaultSettings()
                    }
                )
            }

            SettingsTitle(text = stringResource(R.string.experimental))

            SwitchPreferenceView(
                title = stringResource(R.string.enable_list_tabs),
                subtitle = stringResource(R.string.enable_list_tabs_subtitle),
                value = uiState.useListTabs,
                onValueChange = {
                    event?.setUseListTabs(it)
                }
            )

            SwitchPreferenceView(
                title = stringResource(R.string.always_load_characters),
                value = uiState.loadCharacters,
                onValueChange = { event?.setLoadCharacters(it) }
            )
            SwitchPreferenceView(
                title = stringResource(R.string.random_button_on_list),
                value = uiState.randomListEntryEnabled,
                onValueChange = { event?.setRandomListEntryEnabled(it) }
            )
        }
    }
}

@Composable
fun SettingsTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(start = 72.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview
@Composable
fun SettingsPreview() {
    MoeListTheme {
        Surface {
            SettingsViewContent(
                uiState = SettingsUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}