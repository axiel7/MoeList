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
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.axiel7.moelist.R

object ContextExtensions {

    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Context.openAction(uri: String) {
        Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            startActivity(this)
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
            val browsers = findBrowserIntentActivities()
            val default = browsers.find { it.isDefault }
            if (default != null) {
                setPackage(default.activityInfo.packageName)
                startActivity(this)
            } else {
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
    private fun Context.findBrowserIntentActivities(): List<ResolveInfo> {
        val emptyBrowserIntent = Intent(Intent.ACTION_VIEW, Uri.fromParts("http", "", null))
            .setAction(Intent.ACTION_VIEW)

        return packageManager.queryIntentActivitiesCompat(emptyBrowserIntent, PackageManager.MATCH_ALL)
    }

    /** Custom compat method until Google decides to make one */
    private fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Int = 0) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") queryIntentActivities(intent, flags)
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