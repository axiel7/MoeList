package com.axiel7.moelist.uicompose.composables

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.theme.MoeListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopAppBar(
    title: String,
    navController: NavController
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "back")
            }
        }
    )
}

@Preview
@Composable
fun DefaultTopAppBarPreview() {
    MoeListTheme {
        DefaultTopAppBar(title = "MoeList", navController = rememberNavController())
    }
}