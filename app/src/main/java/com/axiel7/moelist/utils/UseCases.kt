package com.axiel7.moelist.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.login.LoginActivity
import com.axiel7.moelist.utils.ContextExtensions.showToast

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
        showToast(getString(R.string.log_in_again))
    }

    fun String.copyToClipBoard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("title", this))
        context.showToast(context.getString(R.string.copied))
    }
}