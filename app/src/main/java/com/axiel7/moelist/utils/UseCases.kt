package com.axiel7.moelist.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.login.LoginActivity

object UseCases {

    fun Context.logOut() {
        SharedPrefsHelpers.instance?.apply {
            saveBoolean("user_logged", false)
            deleteValue("access_token")
            deleteValue("refresh_token")
        }
        Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(this)
        }
        Toast.makeText(this, getString(R.string.log_in_again), Toast.LENGTH_SHORT).show()
    }

    fun String.copyToClipBoard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("title", this))
        Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
    }
}