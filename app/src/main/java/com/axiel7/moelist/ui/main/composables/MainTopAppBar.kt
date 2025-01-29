package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.Route

@Composable
fun MainTopAppBar(
    profilePicture: String?,
    isVisible: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = isVisible,
        transitionSpec = {
            slideInVertically(initialOffsetY = { -it }) togetherWith
                    slideOutVertically(targetOffsetY = { -it })
        }
    ) { isVisible ->
        if (isVisible) {
            Card(
                onClick = dropUnlessResumed { navController.navigate(Route.Search()) },
                modifier = modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_search_24),
                        contentDescription = "search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.search),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "profile",
                        placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                        error = painterResource(R.drawable.ic_round_account_circle_24),
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .size(32.dp)
                            .clickable { navController.navigate(Route.Profile) }
                    )
                }
            }//:Card
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}
