package com.axiel7.moelist.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.profile.composables.UserStatsView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.MAL_PROFILE_URL
import com.axiel7.moelist.utils.StringExtensions.toNavArgument
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

const val PROFILE_DESTINATION = "profile"

@Composable
fun ProfileView(
    viewModel: ProfileViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateToFullPoster: (String) -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val picture by viewModel.profilePictureUrl.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.user == null) viewModel.getMyUser()
    }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.title_profile),
        navigateBack = navigateBack
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = picture,
                    contentDescription = "profile",
                    placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                    error = painterResource(R.drawable.ic_round_account_circle_24),
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(100))
                        .size(100.dp)
                        .defaultPlaceholder(visible = viewModel.isLoading)
                        .clickable {
                            navigateToFullPoster(
                                arrayOf(picture.orEmpty()).toNavArgument()
                            )
                        }
                )

                Column {
                    Text(
                        text = viewModel.user?.name ?: "Loading...",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading),
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
                            text = birthday.parseDateAndLocalize().orEmpty(),
                            icon = R.drawable.ic_round_cake_24,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    TextIconHorizontal(
                        text = if (viewModel.user?.joinedAt != null)
                            viewModel.user?.joinedAt?.parseDateAndLocalize(
                                inputFormat = DateTimeFormatter.ISO_DATE_TIME
                            ).orEmpty()
                        else "Loading...",
                        icon = R.drawable.ic_round_access_time_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = viewModel.isLoading)
                    )
                }
            }//:Row

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            //Stats
            UserStatsView(
                viewModel = viewModel,
                mediaType = MediaType.ANIME,
                isLoading = viewModel.isLoading
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            UserStatsView(
                viewModel = viewModel,
                mediaType = MediaType.MANGA,
                isLoading = viewModel.isLoadingManga
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            TextButton(
                onClick = { context.openLink(MAL_PROFILE_URL + viewModel.user?.name) },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.view_profile_mal),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }//:Column
    }//:Scaffold
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    MoeListTheme {
        ProfileView(
            navigateBack = {},
            navigateToFullPoster = {}
        )
    }
}