package com.axiel7.moelist.ui.more

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.axiel7.moelist.databinding.FragmentMoreBinding
import com.axiel7.moelist.ui.base.BaseFragment

class MoreFragment : BaseFragment<FragmentMoreBinding>(), SharedPreferences.OnSharedPreferenceChangeListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMoreBinding
        get() = FragmentMoreBinding::inflate
    private lateinit var sharedPreferences: SharedPreferences

    override fun setup() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(safeContext.applicationContext)

        childFragmentManager.beginTransaction()
            .replace(binding.preferencesContainer.id, MoreHomeFragment())
            .commit()

        binding.toolbarSettings.setNavigationOnClickListener { childFragmentManager.popBackStack() }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "theme" || key == "app_language") {
            activity?.recreate()
        }
    }

    fun navigate(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(binding.preferencesContainer.id, fragment)
            .addToBackStack(fragment.tag)
            .commit()
    }

    fun showToolbar() {
        binding.appbarSettings.visibility = View.VISIBLE
    }

    fun hideToolbar() {
        binding.appbarSettings.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
}