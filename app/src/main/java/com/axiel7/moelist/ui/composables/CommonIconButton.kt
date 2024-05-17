package com.axiel7.moelist.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.utils.ContextExtensions.openShareSheet

@Composable
fun BackIconButton(
    onClick: () -> Unit
) {
    IconButton(onClick = dropUnlessResumed { onClick() }) {
        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "arrow_back")
    }
}

@Composable
fun ViewInBrowserButton(
    onClick: () -> Unit
) {
    IconButton(onClick = dropUnlessResumed { onClick() }) {
        Icon(
            painter = painterResource(R.drawable.ic_open_in_browser),
            contentDescription = stringResource(R.string.view_on_mal)
        )
    }
}

@Composable
fun ShareButton(
    url: String
) {
    val context = LocalContext.current
    IconButton(onClick = { context.openShareSheet(url) }) {
        Icon(
            painter = painterResource(R.drawable.round_share_24),
            contentDescription = stringResource(R.string.share)
        )
    }
}