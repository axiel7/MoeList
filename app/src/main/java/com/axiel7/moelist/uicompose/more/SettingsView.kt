package com.axiel7.moelist.uicompose.more

import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GRID_ITEMS_PER_ROW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LANG_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LOAD_CHARACTERS_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.NSFW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.RANDOM_LIST_ENTRY_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.START_TAB_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.THEME_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.TITLE_LANG_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_LIST_TABS_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.uicompose.base.AppLanguage
import com.axiel7.moelist.uicompose.base.BottomDestination
import com.axiel7.moelist.uicompose.base.ItemsPerRow
import com.axiel7.moelist.uicompose.base.ListStyle
import com.axiel7.moelist.uicompose.base.ThemeStyle
import com.axiel7.moelist.uicompose.base.stringRes
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.composables.preferences.ListPreferenceView
import com.axiel7.moelist.uicompose.composables.preferences.PlainPreferenceView
import com.axiel7.moelist.uicompose.composables.preferences.SwitchPreferenceView
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.toInt
import com.axiel7.moelist.utils.UseCases

const val SETTINGS_DESTINATION = "settings"

val themeEntries = ThemeStyle.entries.associate { it.name.lowercase() to it.stringRes }
val languageEntries = AppLanguage.entries.associate { it.value to it.stringResNative }
val listStyleEntries = ListStyle.entries.associate { it.value to it.stringRes }
val itemsPerRowEntries = ItemsPerRow.entries.associate { it.value.toString() to it.stringRes }
val titleLanguageEntries = TitleLanguage.entries.associate { it.name to it.stringRes }
val startTabEntries = mapOf(
    "last_used" to R.string.last_used,
    BottomDestination.Home.value to R.string.title_home,
    BottomDestination.AnimeList.value to R.string.title_anime_list,
    BottomDestination.MangaList.value to R.string.title_manga_list,
    BottomDestination.More.value to R.string.more
)

@Composable
fun SettingsView(
    navigateToListStyleSettings: () -> Unit,
    navigateBack: () -> Unit,
) {
    val context = LocalContext.current
    var langPreference by rememberPreference(LANG_PREFERENCE_KEY, AppLanguage.FOLLOW_SYSTEM.value)
    var themePreference by rememberPreference(
        THEME_PREFERENCE_KEY,
        ThemeStyle.FOLLOW_SYSTEM.name.lowercase()
    )
    var nsfwPreference by rememberPreference(NSFW_PREFERENCE_KEY, false)
    var useGeneralListStyle by rememberPreference(
        USE_GENERAL_LIST_STYLE_PREFERENCE_KEY,
        App.useGeneralListStyle
    )
    var generalListStylePreference by rememberPreference(
        GENERAL_LIST_STYLE_PREFERENCE_KEY,
        ListStyle.STANDARD.value
    )
    var itemsPerRowPreference by rememberPreference(
        GRID_ITEMS_PER_ROW_PREFERENCE_KEY,
        App.gridItemsPerRow
    )
    var startTabPreference by rememberPreference(START_TAB_PREFERENCE_KEY, "last_used")
    var titleLangPreference by rememberPreference(TITLE_LANG_PREFERENCE_KEY, App.titleLanguage.name)
    var useListTabsPreference by rememberPreference(USE_LIST_TABS_PREFERENCE_KEY, App.useListTabs)
    var loadCharactersPreference by rememberPreference(
        LOAD_CHARACTERS_PREFERENCE_KEY,
        App.loadCharacters
    )
    var randomListEntryPreference by rememberPreference(
        RANDOM_LIST_ENTRY_PREFERENCE_KEY,
        App.randomListButton
    )

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
                entriesValues = themeEntries,
                value = themePreference,
                icon = R.drawable.ic_round_color_lens_24,
                onValueChange = { value ->
                    themePreference = value
                }
            )

            ListPreferenceView(
                title = stringResource(R.string.language),
                entriesValues = languageEntries,
                value = langPreference,
                icon = R.drawable.ic_round_language_24,
                onValueChange = { value ->
                    langPreference = value
                    UseCases.changeLocale(value)
                    if (value == "ja") {
                        TitleLanguage.JAPANESE.apply {
                            titleLangPreference = this.name
                            App.titleLanguage = this
                        }
                    }
                }
            )

            ListPreferenceView(
                title = stringResource(R.string.title_language),
                entriesValues = titleLanguageEntries,
                value = titleLangPreference,
                icon = R.drawable.round_title_24,
                onValueChange = { value ->
                    titleLangPreference = value
                    App.titleLanguage = TitleLanguage.valueOf(value)
                }
            )

            ListPreferenceView(
                title = stringResource(R.string.default_section),
                entriesValues = startTabEntries,
                value = startTabPreference,
                icon = R.drawable.ic_round_home_24,
                onValueChange = { value ->
                    startTabPreference = value
                }
            )

            SwitchPreferenceView(
                title = stringResource(R.string.use_separated_list_styles),
                value = !useGeneralListStyle,
                onValueChange = {
                    useGeneralListStyle = !it
                }
            )

            if (useGeneralListStyle) {
                ListPreferenceView(
                    title = stringResource(R.string.list_style),
                    entriesValues = listStyleEntries,
                    value = generalListStylePreference,
                    icon = R.drawable.round_format_list_bulleted_24,
                    onValueChange = { value ->
                        ListStyle.forValue(value)?.let {
                            generalListStylePreference = it.value
                            App.generalListStyle = it
                        }
                    }
                )
            } else {
                PlainPreferenceView(
                    title = stringResource(R.string.list_style),
                    icon = R.drawable.round_format_list_bulleted_24,
                    onClick = navigateToListStyleSettings
                )
            }

            if (generalListStylePreference == ListStyle.GRID.value || !useGeneralListStyle) {
                ListPreferenceView(
                    title = stringResource(R.string.items_per_row),
                    entriesValues = itemsPerRowEntries,
                    value = itemsPerRowPreference.toString(),
                    icon = R.drawable.round_grid_view_24,
                    onValueChange = { value ->
                        value.toIntOrNull()?.let {
                            itemsPerRowPreference = it
                            App.gridItemsPerRow = it
                        }
                    }
                )
            }

            SettingsTitle(text = stringResource(R.string.content))

            SwitchPreferenceView(
                title = stringResource(R.string.show_nsfw),
                subtitle = stringResource(R.string.nsfw_summary),
                value = nsfwPreference,
                icon = R.drawable.no_adult_content_24,
                onValueChange = { value ->
                    nsfwPreference = value
                    App.nsfw = value.toInt()
                }
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
                value = useListTabsPreference,
                onValueChange = {
                    useListTabsPreference = it
                    context.showToast(
                        context.getString(R.string.changes_will_take_effect_on_app_restart)
                    )
                }
            )

            SwitchPreferenceView(
                title = "Always load characters",
                value = loadCharactersPreference,
                onValueChange = {
                    loadCharactersPreference = it
                    App.loadCharacters = it
                }
            )
            SwitchPreferenceView(
                title = "Random button on anime/manga list",
                value = randomListEntryPreference,
                onValueChange = {
                    randomListEntryPreference = it
                    App.randomListButton = it
                }
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