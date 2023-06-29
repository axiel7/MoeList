package com.axiel7.moelist.uicompose.login

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.repository.LoginRepository
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import com.axiel7.moelist.utils.ContextExtensions.showToast

@Composable
fun LoginView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var useExternalBrowser by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.please_login_to_use_this_feature),
                modifier = Modifier.padding(8.dp),
            )

            Button(
                onClick = {
                    context.openLoginUrl(
                        loginUrl = LoginRepository.loginUrl,
                        useExternalBrowser = useExternalBrowser
                    )
                },
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.login),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Row(
                modifier = Modifier.clickable {
                    useExternalBrowser = !useExternalBrowser
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = useExternalBrowser,
                    onCheckedChange = { useExternalBrowser = it },
                )
                Text(text = stringResource(R.string.use_external_browser))
            }
        }
    }//:Scaffold
}

fun Context.openLoginUrl(
    loginUrl: String,
    useExternalBrowser: Boolean
) {
    if (useExternalBrowser) {
        Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl)).apply {
            try {
                showToast(getString(R.string.login_browser_warning))
                startActivity(this)
            } catch (e: ActivityNotFoundException) {
                showToast("No app found for this action")
            }
        }
    } else openCustomTab(loginUrl)
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MoeListTheme {
        LoginView()
    }
}