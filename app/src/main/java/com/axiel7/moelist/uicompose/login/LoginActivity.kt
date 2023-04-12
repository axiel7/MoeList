package com.axiel7.moelist.uicompose.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import com.axiel7.moelist.utils.Extensions.getActivity
import com.axiel7.moelist.utils.Extensions.openCustomTab

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
            Toast.makeText(this, getString(R.string.login_browser_warning), Toast.LENGTH_LONG).show()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    viewModel: LoginViewModel
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var useExternalBrowser by remember { mutableStateOf(false) }

    fun openLogin() {
        if (useExternalBrowser) {
            Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.loginUrl)).apply {
                try {
                    context.startActivity(this)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No app found for this action", Toast.LENGTH_SHORT).show()
                }
            }
        } else context.openCustomTab(viewModel.loginUrl)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Checkbox(
                                    checked = useExternalBrowser,
                                    onCheckedChange = { useExternalBrowser = it }
                                )
                                Text(text = stringResource(R.string.use_external_browser))
                            },
                            onClick = { useExternalBrowser = !useExternalBrowser }
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
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
    }

    if (viewModel.showMessage) {
        Toast.makeText(context, viewModel.message, Toast.LENGTH_SHORT).show()
        viewModel.showMessage = false
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginPreview() {
    MoeListTheme {
        LoginView(viewModel = viewModel())
    }
}