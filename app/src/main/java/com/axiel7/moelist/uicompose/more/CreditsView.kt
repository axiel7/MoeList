package com.axiel7.moelist.uicompose.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopBar
import com.axiel7.moelist.uicompose.composables.DefaultTopAppBar
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants.GENERAL_HELP_CREDIT_URL
import com.axiel7.moelist.utils.Constants.LOGO_CREDIT_URL
import com.axiel7.moelist.utils.ContextExtensions.openLink

const val CREDITS_DESTINATION = "credits"

val translationsCredits = mapOf(
    R.string.ukrainian to "@Sensetivity",
    R.string.turkish to "@hsinankirdar",
    R.string.brazilian to "@RickyM7, @SamOak",
    R.string.russian to "@grin3671",
    R.string.arabic to "@sakugaky, @WhiteCanvas, @Comikazie",
    R.string.german to "@Secresa, @MaximilianGT500",
    R.string.bulgarian to "@itzlighter",
    R.string.french to "@mamanamgae, @frosqh",
    R.string.chinese_traditional to "@jhih_yu_lin",
    R.string.chinese_simplified to "@bengerlorf",
    R.string.japanese to "@axiel7, @Ulong32, @watashibeme",
    R.string.spanish to "@axiel7",
)

@Composable
fun CreditsView(
    navController: NavController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    DefaultScaffoldWithTopBar(
        title = stringResource(R.string.credits),
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
        ) {
            Divider()
            SettingsTitle(text = stringResource(R.string.support))
            MoreItem(
                title = stringResource(R.string.logo_design),
                subtitle = "@danielvd_art",
                onClick = {
                    context.openLink(LOGO_CREDIT_URL)
                }
            )
            MoreItem(
                title = stringResource(R.string.general_help),
                subtitle = "@Jeluchu",
                onClick = {
                    context.openLink(GENERAL_HELP_CREDIT_URL)
                }
            )
            MoreItem(
                title = stringResource(R.string.api_help),
                subtitle = "@Glodanif",
                onClick = {
                    context.openLink(LOGO_CREDIT_URL)
                }
            )
            Divider()
            SettingsTitle(text = stringResource(R.string.translations))
            translationsCredits.forEach { (stringRes, credit) ->
                MoreItem(
                    title = stringResource(stringRes),
                    subtitle = credit,
                    onClick = { }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreditsPreview() {
    MoeListTheme {
        CreditsView(
            navController = rememberNavController()
        )
    }
}