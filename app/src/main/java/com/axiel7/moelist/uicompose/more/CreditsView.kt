package com.axiel7.moelist.uicompose.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.more.composables.MoreItem
import com.axiel7.moelist.uicompose.more.settings.SettingsTitle
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.GENERAL_HELP_CREDIT_URL
import com.axiel7.moelist.utils.LOGO_CREDIT_URL

const val CREDITS_DESTINATION = "credits"

val contributorsCredits = mapOf(
    "@uragiristereo" to "https://github.com/uragiristereo",
    "@krishnapandey24" to "https://github.com/krishnapandey24",
)

val translationsCredits = mapOf(
    R.string.arabic to "@sakugaky, @WhiteCanvas, @Comikazie, @mlvin, @bobteen1",
    R.string.bulgarian to "@itzlighter",
    R.string.czech to "@J4kub07, @gxs3lium",
    R.string.german to "@Secresa, @MaximilianGT500",
    R.string.spanish to "@axiel7",
    R.string.french to "@mamanamgae, @frosqh, @Eria78, @nesquick",
    R.string.indonesian to "@Clxf12",
    R.string.japanese to "@axiel7, @Ulong32, @watashibeme",
    R.string.brazilian to "@RickyM7, @SamOak",
    R.string.portuguese to "@SamOak, @DemiCool",
    R.string.russian to "@grin3671",
    R.string.slovak to "@gxs3lium",
    R.string.turkish to "@hsinankirdar, @kyoya",
    R.string.ukrainian to "@Sensetivity",
    R.string.chinese_simplified to "@bengerlorf",
    R.string.chinese_traditional to "@jhih_yu_lin",
)

@Composable
fun CreditsView(
    navigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.credits),
        navigateBack = navigateBack
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
        ) {
            HorizontalDivider()
            SettingsTitle(text = stringResource(R.string.support))
            MoreItem(
                title = stringResource(R.string.logo_design),
                subtitle = "@danielvd_art",
                onClick = {
                    context.openLink(LOGO_CREDIT_URL)
                }
            )
            MoreItem(
                title = stringResource(R.string.new_logo_design),
                subtitle = "@WSTxda",
                onClick = {
                    context.openLink("https://www.instagram.com/wstxda/")
                }
            )
            MoreItem(
                title = stringResource(R.string.website),
                subtitle = "@MaximilianGT500",
                onClick = {
                    context.openLink("https://github.com/MaximilianGT500")
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
                onClick = {}
            )
            HorizontalDivider()
            SettingsTitle(text = stringResource(R.string.contributors))
            contributorsCredits.forEach { (username, link) ->
                MoreItem(
                    title = username,
                    onClick = {
                        context.openLink(link)
                    }
                )
            }
            HorizontalDivider()
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
            navigateBack = {}
        )
    }
}