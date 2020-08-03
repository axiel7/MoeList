package com.axiel7.moelist.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.axiel7.moelist.MainActivity
import com.axiel7.moelist.R
import com.axiel7.moelist.model.AccessToken
import com.axiel7.moelist.private.ClientId
import com.axiel7.moelist.rest.LoginService
import com.axiel7.moelist.rest.ServiceGenerator
import com.axiel7.moelist.utils.PkceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : Activity() {

    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String

    companion object {
        const val clientId = ClientId.clientId
        const val redirectUri = "moelist://moelist.page.link/"
        const val state = "MoeList123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setDecorFitsSystemWindows(false)

        codeVerifier = PkceGenerator.generateVerifier(128)
        codeChallenge = codeVerifier

        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(resources.getColor(R.color.colorPrimary))
            val chromeIntent = builder.build()
            chromeIntent.intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            chromeIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            chromeIntent.launchUrl(this, Uri.parse("https://myanimelist.net" + "/v1/oauth2/authorize" + "?response_type=code"
                    + "&client_id=" + clientId + "&code_challenge=" + codeVerifier + "&state=" + state))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val uri = intent?.data
        if (uri!=null && uri.toString().startsWith(redirectUri)) {
            val code = uri.getQueryParameter("code")
            val receivedState = uri.getQueryParameter("state")
            if (code!=null && receivedState==state) {
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
                            val sharedPref = this@LoginActivity.getSharedPreferences(getString(R.string.shared_preferences) ,Context.MODE_PRIVATE) ?: return
                            with (sharedPref.edit()) {
                                putString("accessToken", accessToken?.access_token)
                                putString("refreshToken", accessToken?.refresh_token)
                                putBoolean("isUserLogged", true)
                                apply()
                            }
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
                Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}