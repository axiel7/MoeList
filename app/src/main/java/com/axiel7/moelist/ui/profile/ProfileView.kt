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
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.profile.composables.UserStatsView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.MAL_PROFILE_URL
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ProfileView(
    navActionManager: NavActionManager
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager
    )
}

@Composable
private fun ProfileViewContent(
    uiState: ProfileUiState,
    event: ProfileEvent?,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.title_profile),
        navigateBack = navActionManager::goBack
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
                    model = uiState.profilePictureUrl,
                    contentDescription = "profile",
                    placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                    error = painterResource(R.drawable.ic_round_account_circle_24),
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(100))
                        .size(100.dp)
                        .defaultPlaceholder(visible = uiState.isLoading)
                        .clickable(onClick = dropUnlessResumed {
                            navActionManager.toFullPoster(
                                listOf(uiState.profilePictureUrl.orEmpty())
                            )
                        })
                )

                Column {
                    Text(
                        text = uiState.user?.name ?: "Loading...",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    uiState.user?.location?.let { location ->
                        if (location.isNotBlank())
                            TextIconHorizontal(
                                text = location,
                                icon = R.drawable.ic_round_location_on_24,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                    }

                    uiState.user?.birthday?.let { birthday ->
                        TextIconHorizontal(
                            text = birthday.parseDateAndLocalize().orEmpty(),
                            icon = R.drawable.ic_round_cake_24,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    TextIconHorizontal(
                        text = if (uiState.user?.joinedAt != null)
                            uiState.user.joinedAt.parseDateAndLocalize(
                                inputFormat = DateTimeFormatter.ISO_DATE_TIME
                            ).orEmpty()
                        else "Loading...",
                        icon = R.drawable.ic_round_access_time_24,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
            }//:Row

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            //Stats
            UserStatsView(
                uiState = uiState,
                mediaType = MediaType.ANIME,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            UserStatsView(
                uiState = uiState,
                mediaType = MediaType.MANGA,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            TextButton(
                onClick = { context.openLink(MAL_PROFILE_URL + uiState.user?.name) },
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

@Preview
@Composable
fun ProfilePreview() {
    MoeListTheme {
        Surface {
            ProfileViewContent(
                uiState = ProfileUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}