package com.axiel7.moelist.utils

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import com.axiel7.moelist.R

object Extensions {

    /**
     * @return if true 1 else 0
     */
    fun Boolean?.toInt(): Int = if (this == true) 1 else 0

    /**
     * Returns a string representation of the object.
     * Can be called with a null receiver, in which case it returns null.
     */
    fun Any?.toStringOrNull() : String? {
        val result = this.toString()
        return if (result == "null") null else result
    }

    fun Activity.changeTheme() {
        SharedPrefsHelpers.instance?.let {
            when (it.getString("theme", "follow_system")) {
                "light" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    setTheme(R.style.AppTheme)
                }
                "dark" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    setTheme(R.style.AppTheme)
                }
                "follow_system" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    setTheme(R.style.AppTheme)
                }
                "amoled" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    setTheme(R.style.AppTheme_Amoled)
                }
                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    setTheme(R.style.AppTheme)
                }
            }
        }
    }

    /** Shows the soft input keyboard on a SearchView */
    fun SearchView.showKeyboard(context: Context) {
        requestFocus()
        (context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_HIDDEN)
        }
        //inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    /** Open link in Chrome Custom Tabs */
    fun Context.openCustomTab(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(url))
    }
}