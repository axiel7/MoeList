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
import com.axiel7.moelist.model.AccessToken2
import com.axiel7.moelist.rest.LoginService
import com.axiel7.moelist.utils.PkceGenerator
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : Activity() {

    //private lateinit var tinyDB: TinyDB
    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String

    companion object {
        const val clientId = "###"
        const val redirectUri = "moelist://moelist.page.link/"
        const val state = "MoeList123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setDecorFitsSystemWindows(false)

        //tinyDB = TinyDB(this)

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

    override fun onResume() {
        super.onResume()

        val uri = intent.data
        if (uri!=null && uri.toString().startsWith(redirectUri)) {
            val code = uri.getQueryParameter("code")
            val receivedState = uri.getQueryParameter("state")
            Log.d("MoeLog", code.orEmpty())
            if (code!=null && receivedState==state) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .protocols(listOf(Protocol.HTTP_1_1))
                    .build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://myanimelist.net")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val loginService = retrofit.create(LoginService::class.java)
                val call :Call<AccessToken2> = loginService.getAccessToken(clientId, code, codeVerifier,"authorization_code")
                var accessToken: AccessToken2?
                accessToken = null
                call.enqueue(object :Callback<AccessToken2>{

                    override fun onResponse(call: Call<AccessToken2>, response: Response<AccessToken2>) {
                        Log.d("MoeLog", response.message())
                        Log.d("MoeLog", response.errorBody().toString())
                        Log.d("MoeLog", response.toString())
                        accessToken = response.body()
                    }

                    override fun onFailure(call: Call<AccessToken2>, t: Throwable) {
                        Log.d("MoeLog", t.toString())
                    }

                })
                val token = accessToken?.accessToken
                if (token != null) {
                    Log.d("MoeLog", token)
                    //tinyDB.putString("accessToken", accessToken?.accessToken)
                    //tinyDB.putString("refreshToken", accessToken?.refreshToken)
                    //tinyDB.putString("tokenType", accessToken?.tokenType)
                    //tinyDB.putString("authCode", code)
                    //tinyDB.putBoolean("isUserLogged", true)
                    val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
                    with (sharedPref.edit()) {
                        putString("accessToken", accessToken?.accessToken)
                        putString("refreshToken", accessToken?.refreshToken)
                        putBoolean("isUserLogged", true)
                        apply()
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                else { Log.d("MoeLog", "token was null") }
            }
            else if (uri.getQueryParameter("error")!=null) {
                Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}