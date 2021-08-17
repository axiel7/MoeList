package com.axiel7.moelist.ui.more

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.main.MainActivity
import com.google.android.material.transition.MaterialFade

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideToolbar()
        (activity as? MainActivity)?.hideBottomBar()
        (parentFragment as? MoreFragment)?.showToolbar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFade()
        exitTransition = MaterialFade()
        returnTransition = MaterialFade()
        reenterTransition = MaterialFade()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

    }
}