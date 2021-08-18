package com.axiel7.moelist.ui.more

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.main.MainActivity
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialFadeThrough

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showToolbar(false)
        (activity as? MainActivity)?.showBottomBar(false)
        (parentFragment as? MoreFragment)?.showToolbar()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

    }
}