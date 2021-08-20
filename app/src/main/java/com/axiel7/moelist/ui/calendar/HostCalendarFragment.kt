package com.axiel7.moelist.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.FragmentHostCalendarBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.utils.Constants
import com.google.android.material.tabs.TabLayoutMediator

class HostCalendarFragment : BaseFragment<FragmentHostCalendarBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHostCalendarBinding
        get() = FragmentHostCalendarBinding::inflate

    override fun setup() {
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        binding.listViewPager.adapter = CalendarPagerAdapter(childFragmentManager, lifecycle)

        TabLayoutMediator(binding.tabLayout, binding.listViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.monday)
                1 -> tab.text = getString(R.string.tuesday)
                2 -> tab.text = getString(R.string.wednesday)
                3 -> tab.text = getString(R.string.thursday)
                4 -> tab.text = getString(R.string.friday)
                5 -> tab.text = getString(R.string.saturday)
                6 -> tab.text = getString(R.string.sunday)
            }
        }.attach()

    }

    inner class CalendarPagerAdapter(
        fm: FragmentManager,
        lf: Lifecycle
    ) : FragmentStateAdapter(fm, lf) {
        override fun getItemCount(): Int = 7

        override fun createFragment(position: Int): Fragment {
            val fragment = CalendarFragment()
            val bundle = Bundle()
            when (position) {
                0 -> bundle.putString("week_day", Constants.MONDAY)
                1 -> bundle.putString("week_day", Constants.TUESDAY)
                2 -> bundle.putString("week_day", Constants.WEDNESDAY)
                3 -> bundle.putString("week_day", Constants.THURSDAY)
                4 -> bundle.putString("week_day", Constants.FRIDAY)
                5 -> bundle.putString("week_day", Constants.SATURDAY)
                6 -> bundle.putString("week_day", Constants.SUNDAY)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}