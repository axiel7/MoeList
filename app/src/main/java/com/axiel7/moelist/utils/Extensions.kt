package com.axiel7.moelist.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    fun View.showKeyboard() = ViewCompat.getWindowInsetsController(this)
        ?.show(WindowInsetsCompat.Type.ime())

    fun View.hideKeyboard() = ViewCompat.getWindowInsetsController(this)
        ?.hide(WindowInsetsCompat.Type.ime())

    /** Open link in Chrome Custom Tabs */
    fun Context.openCustomTab(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(url))
    }

    /** Aux function with optional values */
    fun TextView.setDrawables(
        @DrawableRes start: Int = 0,
        @DrawableRes top: Int = 0,
        @DrawableRes end: Int = 0,
        @DrawableRes bottom: Int = 0
    ) = this.setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}