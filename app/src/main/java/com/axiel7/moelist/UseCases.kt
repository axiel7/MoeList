package com.axiel7.moelist

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.axiel7.moelist.ui.LoginActivity
import com.axiel7.moelist.utils.SharedPrefsHelpers

object UseCases {

    fun logOut(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val sharedPref = SharedPrefsHelpers.instance
        sharedPref?.saveBoolean("isUserLogged", false)
        sharedPref?.deleteValue("accessToken")
        sharedPref?.deleteValue("refreshToken")
        Toast.makeText(context, context.getString(R.string.log_in_again), Toast.LENGTH_SHORT).show()
        context.startActivity(intent)
    }
}