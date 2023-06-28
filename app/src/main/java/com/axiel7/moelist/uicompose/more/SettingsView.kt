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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LANG_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.LIST_DISPLAY_MODE_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.NSFW_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.START_TAB_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.THEME_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.TITLE_LANG_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.USE_LIST_TABS_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.rememberPreference
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.uicompose.base.ListMode
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.toInt
import com.axiel7.moelist.utils.UseCases

const val SETTINGS_DESTINATION = "settings"

@Composable
fun SettingsView(
    navigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel()
    val langPreference = rememberPreference(LANG_PREFERENCE_KEY, "follow_system")
    val themePreference = rememberPreference(THEME_PREFERENCE_KEY, "follow_system")
    val nsfwPreference = rememberPreference(NSFW_PREFERENCE_KEY, false)
    val listModePreference = rememberPreference(LIST_DISPLAY_MODE_PREFERENCE_KEY, ListMode.STANDARD.value)
    val startTabPreference = rememberPreference(START_TAB_PREFERENCE_KEY, "last_used")
    val titleLangPreference = rememberPreference(TITLE_LANG_PREFERENCE_KEY, App.titleLanguage.name)
    val useListTabsPreference = rememberPreference(USE_LIST_TABS_PREFERENCE_KEY, App.useListTabs)

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.settings),
        navigateBack = navigateBack
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            SettingsTitle(text = stringResource(R.string.display))

            ListPreferenceView(
                title = stringResource(R.string.theme),
                entriesValues = viewModel.themeEntries,
                preferenceValue = themePreference,
                icon = R.drawable.ic_round_color_lens_24,
                onValueChange = { value ->
                    themePreference.value = value
                }
            )

            ListPreferenceView(
                title = stringResource(R.string.language),
                entriesValues = viewModel.languageEntries,
                preferenceValue = langPreference,
                icon = R.drawable.ic_round_language_24,
                onValueChange = { value ->
                    langPreference.value = value
                    UseCases.changeLocale(value)
                    if (value == "ja") {
                        TitleLanguage.JAPANESE.apply {
                            titleLangPreference.value = this.name
                            App.titleLanguage = this
                        }
                    }
                }
            )

            ListPreferenceView(
                title = stringResource(R.string.title_language),
                entriesValues = viewModel.titleLanguageEntries,
                preferenceValue = titleLangPreference,
                icon = R.drawable.round_title_24,
                onValueChange = { value ->
                    titleLangPreference.value = value
                    App.titleLanguage = TitleLanguage.valueOf(value)
                }
            )

            ListPreferenceView(
                title = stringResource(R.string.default_section),
                entriesValues = viewModel.startTabEntries,
                preferenceValue = startTabPreference,
                icon = R.drawable.ic_round_home_24,
                onValueChange = { value ->
                    startTabPreference.value = value
                }
            )

            ListPreferenceView(
                title = stringResource(R.string.list_style),
                entriesValues = viewModel.listModeEntries,
                preferenceValue = listModePreference,
                icon = R.drawable.round_format_list_bulleted_24,
                onValueChange = { value ->
                    ListMode.forValue(value)?.value?.let { listModePreference.value = it }
                }
            )

            SettingsTitle(text = stringResource(R.string.content))

            SwitchPreferenceView(
                title = stringResource(R.string.show_nsfw),
                subtitle = stringResource(R.string.nsfw_summary),
                preferenceValue = nsfwPreference,
                icon = R.drawable.no_adult_content_24,
                onValueChange = { value ->
                    nsfwPreference.value = value
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
                preferenceValue = useListTabsPreference,
                onValueChange = {
                    useListTabsPreference.value = it
                    context.showToast("Restart required")
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
    preferenceValue: MutableState<Boolean>,
    @DrawableRes icon: Int? = null,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                preferenceValue.value = !preferenceValue.value
                onValueChange(preferenceValue.value)
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
            checked = preferenceValue.value,
            onCheckedChange = {
                preferenceValue.value = it
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
    preferenceValue: MutableState<String>,
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
                text = stringResource(entriesValues[preferenceValue.value]!!),
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
                                .clickable { preferenceValue.value = entry.key },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferenceValue.value == entry.key,
                                onClick = { preferenceValue.value = entry.key }
                            )
                            Text(text = stringResource(entry.value))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    onValueChange(preferenceValue.value)
                }) {
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
            navigateBack = {}
        )
    }
}