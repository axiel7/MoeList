package com.axiel7.moelist.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.FragmentHostListBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.utils.Constants.STATUS_ALL
import com.axiel7.moelist.utils.Constants.STATUS_COMPLETED
import com.axiel7.moelist.utils.Constants.STATUS_DROPPED
import com.axiel7.moelist.utils.Constants.STATUS_ON_HOLD
import com.axiel7.moelist.utils.Constants.STATUS_PTR
import com.axiel7.moelist.utils.Constants.STATUS_PTW
import com.axiel7.moelist.utils.Constants.STATUS_READING
import com.axiel7.moelist.utils.Constants.STATUS_WATCHING
import com.google.android.material.tabs.TabLayoutMediator

class HostListFragment : BaseFragment<FragmentHostListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHostListBinding
        get() = FragmentHostListBinding::inflate
    private val currentFragment
        get() = childFragmentManager.findFragmentByTag("f${binding.listViewPager.currentItem}")
    private var type = 0

    override fun setup() {
        type = arguments?.let { HostListFragmentArgs.fromBundle(it).type } ?: 0

        binding.listViewPager.adapter = if (type == 0) AnimeListPagerAdapter(childFragmentManager, lifecycle)
        else MangaListPagerAdapter(childFragmentManager, lifecycle)

        TabLayoutMediator(binding.statusTabLayout, binding.listViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = if (type == 0) { getString(R.string.watching) } else { getString(R.string.reading) }
                1 -> tab.text = if (type == 0) { getString(R.string.ptw) } else { getString(R.string.ptr) }
                2 -> tab.text = getString(R.string.completed)
                3 -> tab.text = getString(R.string.on_hold)
                4 -> tab.text = getString(R.string.dropped)
                5 -> tab.text = getString(R.string.all)
            }
        }.attach()

    }

    inner class AnimeListPagerAdapter(
        fm: FragmentManager,
        lf: Lifecycle
    ) : FragmentStateAdapter(fm, lf) {
        override fun getItemCount(): Int = 6

        override fun createFragment(position: Int): Fragment {
            val fragment = AnimeListFragment()
            val bundle = Bundle()
            when (position) {
                0 -> bundle.putString("status", STATUS_WATCHING)
                1 -> bundle.putString("status", STATUS_PTW)
                2 -> bundle.putString("status", STATUS_COMPLETED)
                3 -> bundle.putString("status", STATUS_ON_HOLD)
                4 -> bundle.putString("status", STATUS_DROPPED)
                5 -> bundle.putString("status", STATUS_ALL)
                else -> bundle.putString("status", STATUS_WATCHING)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    inner class MangaListPagerAdapter(
        fm: FragmentManager,
        lf: Lifecycle
    ) : FragmentStateAdapter(fm, lf) {
        override fun getItemCount(): Int = 6

        override fun createFragment(position: Int): Fragment {
            val fragment = MangaListFragment()
            val bundle = Bundle()
            when (position) {
                0 -> bundle.putString("status", STATUS_READING)
                1 -> bundle.putString("status", STATUS_PTR)
                2 -> bundle.putString("status", STATUS_COMPLETED)
                3 -> bundle.putString("status", STATUS_ON_HOLD)
                4 -> bundle.putString("status", STATUS_DROPPED)
                5 -> bundle.putString("status", STATUS_ALL)
                else -> bundle.putString("status", STATUS_READING)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}