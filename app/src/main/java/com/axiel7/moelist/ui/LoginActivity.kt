package com.axiel7.moelist.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.R
import com.axiel7.moelist.model.AccessToken
import com.axiel7.moelist.private.ClientId
import com.axiel7.moelist.rest.LoginService
import com.axiel7.moelist.rest.ServiceGenerator
import com.axiel7.moelist.utils.PkceGenerator
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String
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

        loading_bar.visibility = View.INVISIBLE

        val loginUrl = Uri.parse(Urls.oauthBaseUrl + "authorize" + "?response_type=code"
                + "&client_id=" + clientId + "&code_challenge=" + codeVerifier + "&state=" + state)
        login.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, loginUrl)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.d("MoeLog", e.message?:"")
                Toast.makeText(this, getString(R.string.login_browser_warning), Toast.LENGTH_LONG).show()
            }
        }

        val uri = intent?.data
        if (uri!=null && uri.toString().startsWith(redirectUri)) { getLoginData(uri) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Toast.makeText(this, getString(R.string.login_browser_warning), Toast.LENGTH_LONG).show()
        val uri = intent?.data
        if (uri!=null && uri.toString().startsWith(redirectUri)) { getLoginData(uri) }
    }

    private fun getLoginData(uri: Uri) {
        if (uri.toString().startsWith(redirectUri)) {
            loading_bar.visibility = View.VISIBLE
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

                            MyApplication.loadSavedPrefs()
                            MyApplication.createRetrofit(applicationContext)
                            val openMainActivity = Intent(this@LoginActivity, MainActivity::class.java)
                            openMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            openMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivityForResult(openMainActivity, 2)
                            finish()
                        }
                        else {
                            Log.d("MoeLog", "token was null")
                            Snackbar.make(login_layout, "Token was null", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                        Log.d("MoeLog", t.toString())
                        Snackbar.make(login_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                })
            }
            else if (uri.getQueryParameter("error")!=null) {
                loading_bar.visibility = View.INVISIBLE
                Snackbar.make(login_layout, getString(R.string.login_error), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}