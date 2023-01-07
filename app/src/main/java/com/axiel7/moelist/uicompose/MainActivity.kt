package com.axiel7.moelist.uicompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.theme.MoeListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoeListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeView()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView() {
    Scaffold(
        bottomBar = { BottomNavBar() }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            Text("Home")
        }
    }
}

@Composable
fun BottomNavBar() {
    var selectedItem by remember { mutableStateOf(0) }
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_outline_home_24), contentDescription = stringResource(id = R.string.title_home)) },
            label = { Text(stringResource(id = R.string.title_home)) },
            selected = selectedItem == 0,
            onClick = { selectedItem = 0 }
        )

        NavigationBarItem(
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_outline_local_movies_24), contentDescription = stringResource(id = R.string.title_anime_list)) },
            label = { Text(stringResource(id = R.string.title_anime_list)) },
            selected = selectedItem == 1,
            onClick = { selectedItem = 1 }
        )

        NavigationBarItem(
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_outline_book_24), contentDescription = stringResource(id = R.string.title_manga_list)) },
            label = { Text(stringResource(id = R.string.title_manga_list)) },
            selected = selectedItem == 2,
            onClick = { selectedItem = 2 }
        )

        NavigationBarItem(
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_more_horizontal), contentDescription = stringResource(id = R.string.more)) },
            label = { Text(stringResource(id = R.string.more)) },
            selected = selectedItem == 3,
            onClick = { selectedItem = 3 }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.DEFAULT)
@Composable
fun HomePreview() {
    MoeListTheme {
        HomeView()
    }
}