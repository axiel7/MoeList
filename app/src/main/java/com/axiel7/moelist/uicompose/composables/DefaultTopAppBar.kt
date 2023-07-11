package com.axiel7.moelist.uicompose.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            BackIconButton(onClick = navigateBack)
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DefaultTopAppBarPreview() {
    MoeListTheme {
        DefaultTopAppBar(
            title = "MoeList",
            navigateBack = {}
        )
    }
}