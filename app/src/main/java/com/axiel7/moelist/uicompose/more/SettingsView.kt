package com.axiel7.moelist.uicompose.more

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LANG_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.NSFW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.START_TAB_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.THEME_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.TITLE_LANG_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_GENERAL_LIST_STYLE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_LIST_TABS_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.uicompose.base.AppLanguage
import com.axiel7.moelist.uicompose.base.BottomDestination
import com.axiel7.moelist.uicompose.base.ListStyle
import com.axiel7.moelist.uicompose.base.ThemeStyle
import com.axiel7.moelist.uicompose.base.stringRes
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.toInt
import com.axiel7.moelist.utils.UseCases

const val SETTINGS_DESTINATION = "settings"

val themeEntries = ThemeStyle.values().associate { it.name.lowercase() to it.stringRes }
val languageEntries = AppLanguage.values().associate { it.value to it.stringResNative }
val listStyleEntries = ListStyle.values().associate { it.value to it.stringRes }
val titleLanguageEntries = TitleLanguage.values().associate { it.name to it.stringRes }
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
    var themePreference by rememberPreference(THEME_PREFERENCE_KEY, ThemeStyle.FOLLOW_SYSTEM.name.lowercase())
    var nsfwPreference by rememberPreference(NSFW_PREFERENCE_KEY, false)
    var useGeneralListStyle by rememberPreference(USE_GENERAL_LIST_STYLE_PREFERENCE_KEY, App.useGeneralListStyle)
    var generalListStylePreference by rememberPreference(GENERAL_LIST_STYLE_PREFERENCE_KEY, ListStyle.STANDARD.value)
    var startTabPreference by rememberPreference(START_TAB_PREFERENCE_KEY, "last_used")
    var titleLangPreference by rememberPreference(TITLE_LANG_PREFERENCE_KEY, App.titleLanguage.name)
    var useListTabsPreference by rememberPreference(USE_LIST_TABS_PREFERENCE_KEY, App.useListTabs)

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

@Composable
fun PlainPreferenceView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes icon: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "",
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                )
            }

            Column(
                modifier = if (subtitle != null)
                    Modifier.padding(16.dp)
                else Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }//: Column
        }//: Row
    }//: Row
}

@Composable
fun SwitchPreferenceView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: Boolean,
    @DrawableRes icon: Int? = null,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onValueChange(!value)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "",
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                )
            }

            Column(
                modifier = if (subtitle != null)
                    Modifier.padding(16.dp)
                else Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 14.sp
                    )
                }
            }//: Column
        }//: Row

        Switch(
            checked = value,
            onCheckedChange = {
                onValueChange(it)
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }//: Row
}

@Composable
fun ListPreferenceView(
    title: String,
    entriesValues: Map<String, Int>,
    modifier: Modifier = Modifier,
    value: String,
    @DrawableRes icon: Int? = null,
    onValueChange: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    var openDialog by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { openDialog = true },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "",
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Spacer(modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
            )
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(entriesValues[value]!!),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text(text = title) },
            text = {
                LazyColumn(
                    modifier = Modifier.sizeIn(
                        maxHeight = (configuration.screenHeightDp - 48).dp
                    )
                ) {
                    items(entriesValues.entries.toList()) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onValueChange(entry.key) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value == entry.key,
                                onClick = { onValueChange(entry.key) }
                            )
                            Text(text = stringResource(entry.value))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onValueChange(value)
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        )
    }
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