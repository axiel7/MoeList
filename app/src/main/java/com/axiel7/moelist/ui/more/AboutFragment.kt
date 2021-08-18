package com.axiel7.moelist.ui.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.main.MainActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class AboutFragment : PreferenceFragmentCompat() {

    private lateinit var safeContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showToolbar(false)
        (activity as? MainActivity)?.showBottomBar(false)
        (parentFragment as? MoreFragment)?.showToolbar()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey)

        val version = findPreference<Preference>("version")
        version?.summary = BuildConfig.VERSION_NAME
        var clicks = 0
        version?.setOnPreferenceClickListener {
            if (clicks == 7) {
                Toast.makeText(safeContext, "✧◝(⁰▿⁰)◜✧", Toast.LENGTH_LONG).show()
                clicks = 0
            } else clicks++
            true
        }

        val discord = findPreference<Preference>("discord")
        discord?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://discord.gg/CTv3WdfxHh")
            startActivity(intent)
            true
        }

        val github = findPreference<Preference>("github")
        github?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/axiel7/MoeList")
            startActivity(intent)
            true
        }

        val credits = findPreference<Preference>("credits")
        credits?.setOnPreferenceClickListener {
            (parentFragment as? MoreFragment)?.navigate(CreditsFragment())
            true
        }

        val licenses = findPreference<Preference>("licenses")
        licenses?.setOnPreferenceClickListener {
            startActivity(Intent(safeContext, OssLicensesMenuActivity::class.java))
            true
        }
    }
}