package com.axiel7.moelist.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R

object ContextExtensions {

    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Context.showToast(@StringRes stringRes: Int) {
        showToast(getString(stringRes))
    }

    fun Context.openAction(uri: String) {
        Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            startActivity(this)
        }
    }

    fun Context.openShareSheet(url: String) {
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
            startActivity(Intent.createChooser(this, null))
        }
    }

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

    /** Open external link by default browser or intent chooser */
    fun Context.openLink(url: String) {
        val uri = Uri.parse(url)
        Intent(Intent.ACTION_VIEW, uri).apply {
            val defaultBrowser = findBrowserIntentActivities(PackageManager.MATCH_DEFAULT_ONLY).firstOrNull()
            if (defaultBrowser != null) {
                setPackage(defaultBrowser.activityInfo.packageName)
                startActivity(this)
            } else {
                val browsers = findBrowserIntentActivities(PackageManager.MATCH_ALL)
                val intents = browsers.map {
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage(it.activityInfo.packageName)
                    }
                }
                startActivity(
                    Intent.createChooser(this, getString(R.string.view_on_mal)).apply {
                        putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
                    }
                )
            }
        }
    }

    /** Finds all the browsers installed on the device */
    private fun Context.findBrowserIntentActivities(
        flags: Int = 0
    ): List<ResolveInfo> {
        val emptyBrowserIntent = Intent(Intent.ACTION_VIEW, Uri.fromParts("http", "", null))

        return packageManager
            .queryIntentActivitiesCompat(emptyBrowserIntent, flags)
            .filter { it.activityInfo.packageName != BuildConfig.APPLICATION_ID }
            .sortedBy { it.priority }
    }

    /** Custom compat method until Google decides to make one */
    private fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Int = 0) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        } else {
            queryIntentActivities(intent, flags)
        }

    @RequiresApi(Build.VERSION_CODES.S)
    fun Context.openByDefaultSettings() {
        try {
            // Samsung OneUI 4 bug can't open ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
            val action = if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            } else {
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
            }
            Intent(
                action,
                Uri.parse("package:${packageName}")
            ).apply {
                startActivity(this)
            }
        } catch (e: Exception) {
            showToast(e.message ?: "Error")
        }
    }

    fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

    fun getCurrentLanguageTag() = LocaleListCompat.getAdjustedDefault()[0]?.toLanguageTag()

    fun Context.openInGoogleTranslate(text: String) {
        try {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra("key_text_input", text)
                putExtra("key_text_output", "")
                putExtra("key_language_from", "en")
                putExtra("key_language_to", getCurrentLanguageTag())
                putExtra("key_suggest_translation", "")
                putExtra("key_from_floating_window", false)
                component = ComponentName(
                    "com.google.android.apps.translate",
                    "com.google.android.apps.translate.TranslateActivity"
                )
                startActivity(this)
            }
        } catch (e: ActivityNotFoundException) {
            showToast("Google Translate not installed")
        } catch (e: Exception) {
            Log.d("translate", e.toString())
        }
    }
}