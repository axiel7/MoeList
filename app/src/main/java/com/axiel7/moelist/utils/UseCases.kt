package com.axiel7.moelist.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.datastore.PreferencesDataStore.ACCESS_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.REFRESH_TOKEN_PREFERENCE_KEY
import com.axiel7.moelist.data.datastore.PreferencesDataStore.defaultPreferencesDataStore
import com.axiel7.moelist.utils.ContextExtensions.showToast

object UseCases {

    suspend fun Context.logOut() {
        defaultPreferencesDataStore.edit {
            it.remove(ACCESS_TOKEN_PREFERENCE_KEY)
            it.remove(REFRESH_TOKEN_PREFERENCE_KEY)
        }
        App.createKtorClient(null)
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