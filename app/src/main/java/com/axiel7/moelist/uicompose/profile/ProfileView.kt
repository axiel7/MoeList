package com.axiel7.moelist.uicompose.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.composables.DefaultTopAppBar
import com.axiel7.moelist.uicompose.composables.TextIconHorizontal
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.DateUtils.toISOformat
import com.axiel7.moelist.utils.Extensions.openLink
import com.google.accompanist.placeholder.material.placeholder
import java.time.format.DateTimeFormatter

const val PROFILE_DESTINATION = "profile"

@Composable
fun ProfileView(navController: NavController) {

    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    Scaffold(
        topBar = {
            DefaultTopAppBar(title = stringResource(R.string.title_profile), navController = navController)
        }
    ) {
        Column(
            modifier = Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = viewModel.user?.picture,
                    contentDescription = "profile",
                    placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                    error = painterResource(R.drawable.ic_round_account_circle_24),
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(100))
                        .size(100.dp)
                        .placeholder(visible = viewModel.isLoading)
                )

                Column {
                    Text(
                        text = viewModel.user?.name ?: "Loading...",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .placeholder(visible = viewModel.isLoading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    viewModel.user?.location?.let { location ->
                        TextIconHorizontal(
                            text = location,
                            icon = R.drawable.ic_round_location_on_24,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    viewModel.user?.birthday?.let { birthday ->
                        TextIconHorizontal(
                            text = birthday,
                            icon = R.drawable.ic_round_cake_24,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    TextIconHorizontal(
                        text = if (viewModel.user?.joinedAt != null)
                            viewModel.user?.joinedAt!!.toISOformat(DateTimeFormatter.ISO_DATE_TIME)
                        else "Loading...",
                        icon = R.drawable.ic_round_access_time_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                }
            }//:Row

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            TextButton(
                onClick = { context.openLink(Constants.MAL_PROFILE_URL + viewModel.user?.name) }
            ) {
                Text(
                    text = stringResource(R.string.view_profile_mal),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }//:Column
    }//:Scaffold
    
    LaunchedEffect(Unit) {
        viewModel.getMyUser()
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ProfilePreview() {
    MoeListTheme {
        ProfileView(navController = rememberNavController())
    }
}