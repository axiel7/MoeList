package com.axiel7.moelist.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
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
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            val browser = browserIntentPackageName()
            if (browser != null) {
                setPackage(browser)
                startActivity(this)
            } else {
                startActivity(Intent.createChooser(this, getString(R.string.view_on_mal)))
            }
        }
    }

    /** Finds the default browser package name */
    private fun Context.browserIntentPackageName() : String? {
        val emptyBrowserIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.fromParts("https", "", null))

        val resolveInfos = packageManager.queryIntentActivities(emptyBrowserIntent, 0)
        return (resolveInfos.find { it.isDefault })?.activityInfo?.packageName
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