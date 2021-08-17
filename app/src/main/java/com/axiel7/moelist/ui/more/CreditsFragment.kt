package com.axiel7.moelist.ui.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.main.MainActivity

class CreditsFragment : PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideToolbar()
        (activity as? MainActivity)?.hideBottomBar()
        (parentFragment as? MoreFragment)?.showToolbar()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.credits_preferences, rootKey)

        val dany = findPreference<Preference>("dany")
        dany?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://instagram.com/danielvd_art")
            startActivity(intent)
            true
        }

        val jelu = findPreference<Preference>("jelu")
        jelu?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/Jeluchu")
            startActivity(intent)
            true
        }
    }
}