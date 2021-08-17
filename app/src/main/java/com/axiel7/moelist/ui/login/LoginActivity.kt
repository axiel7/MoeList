package com.axiel7.moelist.ui.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityLoginBinding
import com.axiel7.moelist.ui.base.BaseActivity
import com.axiel7.moelist.ui.main.MainActivity
import com.axiel7.moelist.utils.Constants.MOELIST_PAGELINK
import io.ktor.client.*
import kotlinx.coroutines.flow.collectLatest

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityLoginBinding
        get() = ActivityLoginBinding::inflate
    private val viewModel: LoginViewModel by viewModels()

    override fun setup() {
        binding.loadingBar.visibility = View.INVISIBLE

        binding.login.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.loginUrl))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.d("MoeLog", e.message?:"")
                Toast.makeText(this, getString(R.string.login_browser_warning), Toast.LENGTH_LONG).show()
            }
        }

        if (intent.data?.toString()?.startsWith(MOELIST_PAGELINK) == true) {
            intent.data?.let { parseIntentData(it) }
        }

        launchLifecycleStarted {
            viewModel.accessToken.collectLatest {
                it?.let {
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
            viewModel.getAccessToken(code)
        }
    }
}