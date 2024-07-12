package com.axiel7.moelist.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.axiel7.moelist.utils.ContextExtensions.copyToClipBoard
import com.axiel7.moelist.utils.ContextExtensions.showToast

object TranslateUtils {

    fun Context.openTranslator(text: String) {
        if (!openInTranslateYou(text)
            && !openInDeepLMini(text)
            && !openInDeepL(text)
            && !openInGoogleTranslateMini(text)
            && !openInGoogleTranslate(text)
        ) {
            showToast("No app found for translation")
        }
    }

    private fun Context.openInGoogleTranslate(text: String): Boolean {
        return try {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra("key_text_input", text)
                putExtra("key_text_output", "")
                putExtra("key_language_from", "en")
                putExtra("key_language_to", ContextExtensions.getCurrentLanguageTag())
                putExtra("key_suggest_translation", "")
                putExtra("key_from_floating_window", false)
                component = ComponentName(
                    "com.google.android.apps.translate",
                    "com.google.android.apps.translate.TranslateActivity"
                )
                startActivity(this)
            }
            true
        } catch (e: Exception) {
            Log.d("translate", e.toString())
            false
        }
    }

    private fun Context.openInGoogleTranslateMini(text: String): Boolean {
        return try {
            Intent(Intent.ACTION_PROCESS_TEXT).apply {
                component = ComponentName(
                    "com.google.android.apps.translate",
                    "com.google.android.apps.translate.copydrop.gm3.TapToTranslateActivity"
                )
                type = "text/plain"
                putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                startActivity(this)
            }
            true
        } catch (e: Exception) {
            Log.d("translate", e.toString())
            false
        }
    }

    private fun Context.openInDeepL(text: String): Boolean {
        return try {
            copyToClipBoard(text)
            Intent(Intent.ACTION_VIEW).apply {
                component = ComponentName(
                    "com.deepl.mobiletranslator",
                    "com.deepl.mobiletranslator.MainActivity"
                )
                startActivity(this)
            }
            true
        } catch (e: Exception) {
            Log.d("translate", e.toString())
            false
        }
    }

    private fun Context.openInDeepLMini(text: String): Boolean {
        return try {
            Intent(Intent.ACTION_PROCESS_TEXT).apply {
                component = ComponentName(
                    "com.deepl.mobiletranslator",
                    "com.deepl.mobiletranslator.MiniTranslatorActivity"
                )
                type = "text/plain"
                putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                startActivity(this)
            }
            true
        } catch (e: Exception) {
            Log.d("translate", e.toString())
            false
        }
    }

    private fun Context.openInTranslateYou(text: String): Boolean {
        return try {
            Intent(Intent.ACTION_PROCESS_TEXT).apply {
                component = ComponentName(
                    "com.bnyro.translate",
                    "com.bnyro.translate.ui.ShareActivity"
                )
                type = "text/plain"
                putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                startActivity(this)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}