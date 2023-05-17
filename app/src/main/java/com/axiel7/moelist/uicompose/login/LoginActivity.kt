package com.axiel7.moelist.uicompose.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.MainActivity
import com.axiel7.moelist.uicompose.theme.MoeListTheme
import com.axiel7.moelist.utils.Constants
import com.axiel7.moelist.utils.ContextExtensions.getActivity
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import com.axiel7.moelist.utils.ContextExtensions.showToast

class LoginActivity: AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MoeListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginView(viewModel = viewModel)
                }
            }
        }

        if (intent.data?.toString()?.startsWith(Constants.MOELIST_PAGELINK) == true) {
            intent.data?.let { parseIntentData(it) }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data?.toString()?.startsWith(Constants.MOELIST_PAGELINK) == true) {
            intent.data?.let { parseIntentData(it) }
        }
    }

    private fun parseIntentData(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val receivedState = uri.getQueryParameter("state")
        if (code != null && receivedState == LoginViewModel.STATE) {
            viewModel.getAccessToken(code)
        }
    }
}

@Composable
fun LoginView(
    viewModel: LoginViewModel
) {
    val context = LocalContext.current
    var useExternalBrowser by remember { mutableStateOf(false) }

    fun openLogin() {
        if (useExternalBrowser) {
            Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.loginUrl)).apply {
                try {
                    context.showToast(context.getString(R.string.login_browser_warning))
                    context.startActivity(this)
                } catch (e: ActivityNotFoundException) {
                    context.showToast("No app found for this action")
                }
            }
        } else context.openCustomTab(viewModel.loginUrl)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.moelist_banner),
                contentDescription = "banner",
                modifier = Modifier.padding(bottom = 28.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = { openLogin() },
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

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }

    if (viewModel.loginWasOk) {
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(this)
            context.getActivity()?.finish()
        }
        viewModel.loginWasOk = false
    }

    LaunchedEffect(viewModel.message) {
        if (viewModel.showMessage) {
            context.showToast(viewModel.message)
            viewModel.showMessage = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MoeListTheme {
        LoginView(viewModel = viewModel())
    }
}