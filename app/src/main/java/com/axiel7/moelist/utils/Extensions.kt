package com.axiel7.moelist.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Window
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    setTheme(R.style.AppTheme)
                }
            }
        }
    }

    fun Window.showKeyboard() = WindowInsetsControllerCompat(this, decorView).show(WindowInsetsCompat.Type.ime())

    fun Window.hideKeyboard() = WindowInsetsControllerCompat(this, decorView).hide(WindowInsetsCompat.Type.ime())

    /** Open link in Chrome Custom Tabs */
    fun Context.openCustomTab(url: String) {
        val colors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(this, R.color.colorMoeList))
            .build()
        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colors)
            .build().apply {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchUrl(this@openCustomTab, Uri.parse(url))
            }
    }

    /** Open external link by intent chooser */
    fun Context.openLink(url: String) {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            startActivity(Intent.createChooser(this, url))
        }
    }

    /** Aux function with optional values */
    fun TextView.setDrawables(
        @DrawableRes start: Int = 0,
        @DrawableRes top: Int = 0,
        @DrawableRes end: Int = 0,
        @DrawableRes bottom: Int = 0
    ) = this.setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}