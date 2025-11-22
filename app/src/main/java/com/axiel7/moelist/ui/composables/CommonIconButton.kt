package com.axiel7.moelist.ui.composables

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.utils.ContextExtensions.openShareSheet

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackIconButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = dropUnlessResumed { onClick() },
        shapes = IconButtonDefaults.shapes()
    ) {
        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "arrow_back")
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ViewInBrowserButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = dropUnlessResumed { onClick() },
        shapes = IconButtonDefaults.shapes()
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_open_in_browser),
            contentDescription = stringResource(R.string.view_on_mal)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareButton(
    url: String
) {
    val context = LocalContext.current
    IconButton(
        onClick = { context.openShareSheet(url) },
        shapes = IconButtonDefaults.shapes()
    ) {
        Icon(
            painter = painterResource(R.drawable.round_share_24),
            contentDescription = stringResource(R.string.share)
        )
    }
}