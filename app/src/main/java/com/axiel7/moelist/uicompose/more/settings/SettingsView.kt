package com.axiel7.moelist.uicompose.more.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.axiel7.moelist.uicompose.base.AppLanguage
import com.axiel7.moelist.uicompose.base.ItemsPerRow
import com.axiel7.moelist.uicompose.base.ListStyle
import com.axiel7.moelist.uicompose.base.StartTab
import com.axiel7.moelist.uicompose.base.ThemeStyle
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.preferences.ListPreferenceView
import com.axiel7.moelist.uicompose.composables.preferences.PlainPreferenceView
import com.axiel7.moelist.uicompose.composables.preferences.SwitchPreferenceView
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings
import com.axiel7.moelist.utils.ContextExtensions.showToast
import org.koin.androidx.compose.navigation.koinNavViewModel

const val SETTINGS_DESTINATION = "settings"

@Composable
fun SettingsView(
    viewModel: SettingsViewModel = koinNavViewModel(),
    navigateToListStyleSettings: () -> Unit,
    navigateBack: () -> Unit,
) {
    val context = LocalContext.current

    val lang by viewModel.lang.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val nsfw by viewModel.nsfw.collectAsStateWithLifecycle()
    val useGeneralListStyle by viewModel.useGeneralListStyle.collectAsStateWithLifecycle()
    val generalListStyle by viewModel.generalListStyle.collectAsStateWithLifecycle()
    val itemsPerRow by viewModel.itemsPerRow.collectAsStateWithLifecycle()
    val startTab by viewModel.startTab.collectAsStateWithLifecycle()
    val titleLang by viewModel.titleLang.collectAsStateWithLifecycle()
    val useListTabs by viewModel.useListTabs.collectAsStateWithLifecycle()
    val loadCharacters by viewModel.loadCharacters.collectAsStateWithLifecycle()
    val randomListEntryEnabled by viewModel.randomListEntryEnabled.collectAsStateWithLifecycle()

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.settings),
        navigateBack = navigateBack
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
                value = theme,
                icon = R.drawable.ic_round_color_lens_24,
                onValueChange = viewModel::setTheme
            )

            ListPreferenceView(
                title = stringResource(R.string.language),
                entriesValues = AppLanguage.entriesLocalized,
                value = lang,
                icon = R.drawable.ic_round_language_24,
                onValueChange = viewModel::setLang
            )

            ListPreferenceView(
                title = stringResource(R.string.title_language),
                entriesValues = TitleLanguage.entriesLocalized,
                value = titleLang,
                icon = R.drawable.round_title_24,
                onValueChange = viewModel::setTitleLang
            )

            ListPreferenceView(
                title = stringResource(R.string.default_section),
                entriesValues = StartTab.entriesLocalized,
                value = startTab ?: StartTab.LAST_USED,
                icon = R.drawable.ic_round_home_24,
                onValueChange = viewModel::setStartTab
            )

            SwitchPreferenceView(
                title = stringResource(R.string.use_separated_list_styles),
                value = !useGeneralListStyle,
                onValueChange = {
                    viewModel.setUseGeneralListStyle(!it)
                }
            )

            if (useGeneralListStyle) {
                ListPreferenceView(
                    title = stringResource(R.string.list_style),
                    entriesValues = ListStyle.entriesLocalized,
                    value = generalListStyle,
                    icon = R.drawable.round_format_list_bulleted_24,
                    onValueChange = viewModel::setGeneralListStyle
                )
            } else {
                PlainPreferenceView(
                    title = stringResource(R.string.list_style),
                    icon = R.drawable.round_format_list_bulleted_24,
                    onClick = navigateToListStyleSettings
                )
            }

            if (generalListStyle == ListStyle.GRID || !useGeneralListStyle) {
                ListPreferenceView(
                    title = stringResource(R.string.items_per_row),
                    entriesValues = ItemsPerRow.entriesLocalized,
                    value = itemsPerRow,
                    icon = R.drawable.round_grid_view_24,
                    onValueChange = viewModel::setItemsPerRow
                )
            }

            SettingsTitle(text = stringResource(R.string.content))

            SwitchPreferenceView(
                title = stringResource(R.string.show_nsfw),
                subtitle = stringResource(R.string.nsfw_summary),
                value = nsfw,
                icon = R.drawable.no_adult_content_24,
                onValueChange = viewModel::setNsfw
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
                title = "Enable list tabs",
                subtitle = "Use tabs in Anime/Manga list instead of Floating Action Button",
                value = useListTabs,
                onValueChange = {
                    viewModel.setUseListTabs(it)
                    context.showToast(
                        context.getString(R.string.changes_will_take_effect_on_app_restart)
                    )
                }
            )

            SwitchPreferenceView(
                title = "Always load characters",
                value = loadCharacters,
                onValueChange = viewModel::setLoadCharacters
            )
            SwitchPreferenceView(
                title = "Random button on anime/manga list",
                value = randomListEntryEnabled,
                onValueChange = viewModel::setRandomListEntryEnabled
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

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    MoeListTheme {
        SettingsView(
            navigateToListStyleSettings = {},
            navigateBack = {}
        )
    }
}