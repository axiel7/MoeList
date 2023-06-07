package com.axiel7.moelist.uicompose.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R

@Composable
fun BackIconButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "arrow_back")
    }
}

@Composable
fun ViewInBrowserButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.ic_open_in_browser),
            contentDescription = stringResource(R.string.view_on_mal)
        )
    }
}