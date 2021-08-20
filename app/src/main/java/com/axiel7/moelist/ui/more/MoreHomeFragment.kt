package com.axiel7.moelist.ui.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.main.MainActivity
import com.axiel7.moelist.utils.Extensions.openCustomTab
import com.axiel7.moelist.utils.UseCases.logOut

class MoreHomeFragment : PreferenceFragmentCompat() {

    private lateinit var safeContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showToolbar()
        (activity as? MainActivity)?.showBottomBar()
        (parentFragment as? MoreFragment)?.hideToolbar()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.home_preferences, rootKey)

        val news = findPreference<Preference>("news")
        news?.setOnPreferenceClickListener {
            safeContext.openCustomTab("https://myanimelist.net/news")
            true
        }

        val announcements = findPreference<Preference>("announcements")
        announcements?.setOnPreferenceClickListener {
            safeContext.openCustomTab("https://myanimelist.net/forum/?board=5")
            true
        }

        val setting = findPreference<Preference>("settings")
        setting?.setOnPreferenceClickListener {
            (parentFragment as? MoreFragment)?.navigate(SettingsFragment())
            true
        }

        val about = findPreference<Preference>("about")
        about?.setOnPreferenceClickListener {
            (parentFragment as? MoreFragment)?.navigate(AboutFragment())
            true
        }

        val feedback = findPreference<Preference>("feedback")
        feedback?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("contacto.axiel7@gmail.com"))
            startActivity(intent)
            true
        }

        val logout = findPreference<Preference>("logout")
        logout?.setOnPreferenceClickListener {
            context?.logOut()
            true
        }
    }
}