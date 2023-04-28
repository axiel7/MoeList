package com.axiel7.moelist.uicompose.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.axiel7.moelist.uicompose.composables.DefaultScaffoldWithTopBar
import com.axiel7.moelist.uicompose.composables.DonutChart
import com.axiel7.moelist.uicompose.composables.TextIconHorizontal
import com.axiel7.moelist.uicompose.composables.TextIconVertical
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.DateUtils.deduceDateFormat
import com.axiel7.moelist.utils.DateUtils.parseDate
import com.axiel7.moelist.utils.DateUtils.toLocalized
import com.axiel7.moelist.utils.NumExtensions.toStringOrZero
import com.axiel7.moelist.utils.StringExtensions.toNavArgument
import com.google.accompanist.placeholder.material.placeholder
import java.time.format.DateTimeFormatter

const val PROFILE_DESTINATION = "profile"

@Composable
fun ProfileView(navController: NavController) {

    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    DefaultScaffoldWithTopBar(
        title = stringResource(R.string.title_profile),
        navController = navController
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = viewModel.profilePictureUrl,
                    contentDescription = "profile",
                    placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                    error = painterResource(R.drawable.ic_round_account_circle_24),
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(100))
                        .size(100.dp)
                        .placeholder(visible = viewModel.isLoading)
                        .clickable {
                            navController.navigate(
                                "full_poster/${arrayOf(viewModel.profilePictureUrl ?: "").toNavArgument()}"
                            )
                        }
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
                        if (location.isNotBlank())
                            TextIconHorizontal(
                                text = location,
                                icon = R.drawable.ic_round_location_on_24,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                    }

                    viewModel.user?.birthday?.let { birthday ->
                        TextIconHorizontal(
                            text = birthday.parseDate(
                                inputFormat = birthday.deduceDateFormat()
                            ).toLocalized(),
                            icon = R.drawable.ic_round_cake_24,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    TextIconHorizontal(
                        text = if (viewModel.user?.joinedAt != null)
                            viewModel.user?.joinedAt!!.parseDate(DateTimeFormatter.ISO_DATE_TIME).toLocalized()
                        else "Loading...",
                        icon = R.drawable.ic_round_access_time_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .placeholder(visible = viewModel.isLoading)
                    )
                }
            }//:Row

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            //Stats
            Text(
                text = stringResource(R.string.anime_stats),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    stats = viewModel.animeStats,
                    centerContent = {
                        Text(text = stringResource(R.string.total_entries)
                            .format(viewModel.animeStats.sumOf { it.value.toInt() })
                        )
                    }
                )

                Column {
                    viewModel.animeStats.forEach {
                        SuggestionChip(
                            onClick = { },
                            label = { Text(text = stringResource(it.title)) },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            icon = { Text(text = String.format("%.0f", it.value)) },
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                borderColor = it.color
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TextIconVertical(
                    text = viewModel.user?.animeStatistics?.numDays.toStringOrZero(),
                    icon = R.drawable.ic_round_event_24,
                    tooltip = stringResource(R.string.days)
                )
                TextIconVertical(
                    text = viewModel.user?.animeStatistics?.numEpisodes.toStringOrZero(),
                    icon = R.drawable.play_circle_outline_24,
                    tooltip = stringResource(R.string.episodes)
                )
                TextIconVertical(
                    text = viewModel.user?.animeStatistics?.meanScore.toStringOrZero(),
                    icon = R.drawable.ic_round_details_star_24,
                    tooltip = stringResource(R.string.mean_score)
                )
                TextIconVertical(
                    text = viewModel.user?.animeStatistics?.numTimesRewatched.toStringOrZero(),
                    icon = R.drawable.round_repeat_24,
                    tooltip = stringResource(R.string.rewatched)
                )
            }

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
        if (viewModel.user == null) viewModel.getMyUser()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    MoeListTheme {
        ProfileView(navController = rememberNavController())
    }
}