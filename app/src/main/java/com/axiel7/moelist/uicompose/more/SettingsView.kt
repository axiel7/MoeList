package com.axiel7.moelist.uicompose.more

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.composables.DefaultTopAppBar
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants.APP_LANGUAGE_KEY
import com.axiel7.moelist.utils.SharedPrefsHelpers

const val SETTINGS_DESTINATION = "settings"

@Composable
fun SettingsView(
    navController: NavController
) {
    val viewModel: SettingsViewModel = viewModel()

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = stringResource(R.string.settings),
                navController = navController
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            SettingsTitle(text = stringResource(R.string.display))

            ListPreferenceView(
                title = stringResource(R.string.theme),
                entriesValues = viewModel.themeEntries,
                defaultValue = "follow_system",
                icon = R.drawable.ic_round_color_lens_24,
                onValueChange = { }
            )

            ListPreferenceView(
                title = stringResource(R.string.language),
                entriesValues = viewModel.languageEntries,
                defaultValue = viewModel.getSavedLanguageOption(),
                icon = R.drawable.ic_round_language_24,
                onValueChange = {
                    SharedPrefsHelpers.instance?.apply {
                        saveString(APP_LANGUAGE_KEY, it)
                        val appLocale = LocaleListCompat.forLanguageTags(it)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
            )

            SettingsTitle(text = stringResource(R.string.startup))

            ListPreferenceView(
                title = stringResource(R.string.default_section),
                entriesValues = viewModel.sectionEntries,
                defaultValue = "home",
                icon = R.drawable.ic_round_sort_24,
                onValueChange = { }
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
fun ListPreferenceView(
    title: String,
    entriesValues: Map<String, Int>,
    modifier: Modifier = Modifier,
    defaultValue: String? = null,
    @DrawableRes icon: Int? = null,
    onValueChange: (String?) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    var selectedValue by remember { mutableStateOf(defaultValue) }
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
            modifier = if (selectedValue != null)
                Modifier.padding(16.dp)
            else Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedValue != null) {
                Text(
                    text = stringResource(entriesValues[selectedValue]!!),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text(text = title) },
            text = {
                LazyColumn(
                    modifier = Modifier.sizeIn(maxHeight = 500.dp)
                ) {
                    items(entriesValues.entries.toList()) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedValue = entry.key },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedValue == entry.key,
                                onClick = { selectedValue = entry.key }
                            )
                            Text(text = stringResource(entry.value))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    onValueChange(selectedValue)
                }) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SettingsPreview() {
    MoeListTheme {
        SettingsView(
            navController = rememberNavController()
        )
    }
}