package com.axiel7.moelist.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import com.axiel7.moelist.R
import com.axiel7.moelist.uicompose.login.LoginActivity
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.utils.PreferencesDataStore.defaultPreferencesDataStore

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

    fun Context.copyToClipBoard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("title", text))
        showToast(getString(R.string.copied))
    }

    fun changeLocale(language: String) {
        val appLocale = if (language == "follow_system") LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}