package com.axiel7.moelist.uicompose.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultScaffoldWithTopAppBar(
    title: String,
    navigateBack: () -> Unit,
    floatingActionButton: @Composable (() -> Unit) = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            DefaultTopAppBar(
                title = title,
                scrollBehavior = topAppBarScrollBehavior,
                navigateBack = navigateBack
            )
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}