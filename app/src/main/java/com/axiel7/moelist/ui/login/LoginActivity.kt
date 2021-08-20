package com.axiel7.moelist.ui.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityLoginBinding
import com.axiel7.moelist.ui.base.BaseActivity
import com.axiel7.moelist.ui.main.MainActivity
import com.axiel7.moelist.utils.Constants.MOELIST_PAGELINK
import com.axiel7.moelist.utils.Extensions.openCustomTab
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.ktor.client.*
import kotlinx.coroutines.flow.collectLatest

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityLoginBinding
        get() = ActivityLoginBinding::inflate
    private val viewModel: LoginViewModel by viewModels()
    private val helpDialog: MaterialAlertDialogBuilder by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.help))
            .setMessage(
                HtmlCompat.fromHtml(
                    "<ul><li>I can't login</li><p>Please open the menu and check the option \"Use external browser\"</ul>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            )
            .setPositiveButton(getString(R.string.ok)) { _, _ -> }
    }

    override fun setup() {
        binding.loadingBar.visibility = View.INVISIBLE
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.help -> {
                    helpDialog.show()
                    true
                }
                R.id.external_browser -> {
                    it.isChecked = !it.isChecked
                    viewModel.setUseExternalBrowser(it.isChecked)
                    true
                }
                else -> false
            }
        }

        binding.login.setOnClickListener {
            if (viewModel.userExternalBrowser) {
                Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.loginUrl)).apply {
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        showToast(getString(R.string.login_browser_warning))
                    }
                }
            } else openCustomTab(viewModel.loginUrl)
        }

        if (intent.data?.toString()?.startsWith(MOELIST_PAGELINK) == true) {
            intent.data?.let { parseIntentData(it) }
        }

        launchLifecycleStarted {
            viewModel.accessToken.collectLatest {
                it?.let {
                    if (it.accessToken != null) {
                        sharedPref.apply {
                            saveString("access_token", it.accessToken)
                            saveString("refresh_token", it.refreshToken)
                            saveBoolean("user_logged", true)
                        }
                        App.createKtorClient()
                        Intent(this@LoginActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(this)
                            finish()
                        }
                    } else showToast("Token null: ${it.error}: ${it.message}")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data?.toString()?.startsWith(MOELIST_PAGELINK) == true) {
            Toast.makeText(this, getString(R.string.login_browser_warning), Toast.LENGTH_LONG).show()
            intent.data?.let { parseIntentData(it) }
        }
    }

    private fun parseIntentData(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val receivedState = uri.getQueryParameter("state")
        if (code != null && receivedState == LoginViewModel.STATE) {
            binding.loadingBar.visibility = View.VISIBLE
            viewModel.getAccessToken(code)
        }
    }
}