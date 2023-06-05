package com.axiel7.moelist.uicompose.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants.DISCORD_SERVER_URL
import com.axiel7.moelist.utils.Constants.GITHUB_REPO_URL
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast

const val ABOUT_DESTINATION = "about"

@Composable
fun AboutView(
    navigateBack: () -> Unit,
    navigateToCredits: () -> Unit,
) {
    val context = LocalContext.current
    var versionClicks by remember { mutableIntStateOf(0) }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.about),
        navigateBack = navigateBack
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            MoreItem(
                title = stringResource(R.string.version),
                subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                icon = R.drawable.ic_moelist_logo,
                onClick = {
                    if (versionClicks >= 7) {
                        context.showToast("✧◝(⁰▿⁰)◜✧")
                        versionClicks = 0
                    }
                    else versionClicks++
                }
            )
            MoreItem(
                title = stringResource(R.string.discord),
                subtitle = stringResource(R.string.discord_summary),
                icon = R.drawable.ic_discord,
                onClick = {
                    context.openLink(DISCORD_SERVER_URL)
                }
            )
            MoreItem(
                title = stringResource(R.string.github),
                subtitle = stringResource(R.string.github_summary),
                icon = R.drawable.ic_github,
                onClick = {
                    context.openLink(GITHUB_REPO_URL)
                }
            )
            MoreItem(
                title = stringResource(R.string.credits),
                subtitle = stringResource(R.string.credits_summary),
                icon = R.drawable.ic_round_group_24,
                onClick = navigateToCredits
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    MoeListTheme {
        AboutView(
            navigateBack = {},
            navigateToCredits = {}
        )
    }
}