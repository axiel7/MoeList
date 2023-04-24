package com.axiel7.moelist.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.login.LoginActivity
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.utils.PreferencesDataStore.defaultPreferencesDataStore
import java.util.Locale

object UseCases {

    suspend fun Context.logOut() {
        defaultPreferencesDataStore.edit {
            it[ACCESS_TOKEN_PREFERENCE_KEY] = ""
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

    fun Context.changeLocale(language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return createConfigurationContext(configuration)
    }
}