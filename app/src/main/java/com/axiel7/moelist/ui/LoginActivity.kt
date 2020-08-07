package com.axiel7.moelist.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String
    private lateinit var loadingBar: ProgressBar

    companion object {
        const val clientId = ClientId.clientId
        const val redirectUri = Urls.redirectUri
        const val state = "MoeList123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_login)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }

        codeVerifier = PkceGenerator.generateVerifier(128)
        codeChallenge = codeVerifier

        loadingBar = findViewById(R.id.loading_bar)

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
                var accessToken: AccessToken?
                accessToken = null
                call.enqueue(object :Callback<AccessToken>{

                    override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {

                        accessToken = response.body()
                        val token = accessToken?.access_token
                        if (token != null) {
                            Log.d("MoeLog", "AccessToken=$token")

                            SharedPrefsHelpers.init(this@LoginActivity)
                            val sharedPref = SharedPrefsHelpers.instance
                            sharedPref?.saveString("accessToken", accessToken?.access_token)
                            sharedPref?.saveString("refreshToken", accessToken?.refresh_token)
                            sharedPref?.saveBoolean("isUserLogged", true)

                            val openMainActivity = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(openMainActivity)
                        }
                        else { Log.d("MoeLog", "token was null") }
                    }

                    override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                        Log.d("MoeLog", t.toString())
                    }
                })
            }
            else if (uri.getQueryParameter("error")!=null) {
                loadingBar.visibility = View.INVISIBLE
                Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}