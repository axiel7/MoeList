package com.axiel7.moelist.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.axiel7.moelist.R
import com.axiel7.moelist.model.AccessToken
import com.axiel7.moelist.private.ClientId
import com.axiel7.moelist.rest.LoginService
import com.axiel7.moelist.rest.ServiceGenerator
import com.axiel7.moelist.utils.PkceGenerator
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String
    private lateinit var loadingBar: ProgressBar
    private lateinit var snackBarView: View
    private lateinit var accessToken: AccessToken

    companion object {
        const val clientId = ClientId.clientId
        const val redirectUri = Urls.redirectUri
        const val state = "MoeList123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        codeVerifier = PkceGenerator.generateVerifier(128)
        codeChallenge = codeVerifier

        snackBarView = findViewById(R.id.login_layout)
        loadingBar = findViewById(R.id.loading_bar)
        loadingBar.visibility = View.INVISIBLE

        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
            val chromeIntent = builder.build()
            chromeIntent.intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            chromeIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            chromeIntent.launchUrl(this, Uri.parse(Urls.oauthBaseUrl + "authorize" + "?response_type=code"
                    + "&client_id=" + clientId + "&code_challenge=" + codeVerifier + "&state=" + state
            ))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val uri = intent?.data
        if (uri!=null && uri.toString().startsWith(redirectUri)) {
            loadingBar.visibility = View.VISIBLE
            val code = uri.getQueryParameter("code")
            val receivedState = uri.getQueryParameter("state")
            if (code!=null && receivedState== state) {
                val loginService = ServiceGenerator.createService(LoginService::class.java)
                val call :Call<AccessToken> = loginService.getAccessToken(clientId, code, codeVerifier,"authorization_code")
                call.enqueue(object :Callback<AccessToken>{

                    override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {

                        if (response.isSuccessful && response.body() != null) {
                            accessToken = response.body()!!
                            SharedPrefsHelpers.init(this@LoginActivity)
                            val sharedPref = SharedPrefsHelpers.instance
                            sharedPref?.saveString("accessToken", accessToken.access_token)
                            sharedPref?.saveString("refreshToken", accessToken.refresh_token)
                            sharedPref?.saveBoolean("isUserLogged", true)

                            val openMainActivity = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivityForResult(openMainActivity, 2)
                        }
                        else {
                            Log.d("MoeLog", "token was null")
                            Snackbar.make(snackBarView, "Token was null", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                        Log.d("MoeLog", t.toString())
                        Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                })
            }
            else if (uri.getQueryParameter("error")!=null) {
                loadingBar.visibility = View.INVISIBLE
                Snackbar.make(snackBarView, getString(R.string.login_error), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}