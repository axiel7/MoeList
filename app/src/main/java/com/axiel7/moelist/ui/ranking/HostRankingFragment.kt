package com.axiel7.moelist.ui.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.FragmentHostRankingBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator

class HostRankingFragment : BaseFragment<FragmentHostRankingBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHostRankingBinding
        get() = FragmentHostRankingBinding::inflate
    private var type = 0

    override fun setup() {
        type = arguments?.let { HostRankingFragmentArgs.fromBundle(it).type } ?: 0
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        when (type) {
            0 -> {
                binding.tabLayout.addTab(binding.tabLayout.newTab())
                binding.rankingViewpager.adapter = AnimeRankingPagerAdapter(childFragmentManager, lifecycle)
            }
            1 -> {
                binding.rankingViewpager.adapter = MangaRankingPagerAdapter(childFragmentManager, lifecycle)
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.rankingViewpager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.all)
                1 -> tab.text = getString(R.string.popular)
                2 -> tab.text = getString(R.string.favorite)
                3 -> tab.text = getString(R.string.upcoming)
            }
        }.attach()
    }

    inner class AnimeRankingPagerAdapter(
        fm: FragmentManager,
        lf: Lifecycle
    ) : FragmentStateAdapter(fm, lf) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            val fragment = RankingFragment()
            val bundle = Bundle()
            when (position) {
                0 -> bundle.putString("rank_type", "all")
                1 -> bundle.putString("rank_type", "bypopularity")
                2 -> bundle.putString("rank_type", "favorite")
                3 -> bundle.putString("rank_type", "upcoming")
            }
            bundle.putInt("type", 0)
            fragment.arguments = bundle
            return fragment
        }
    }

    inner class MangaRankingPagerAdapter(
        fm: FragmentManager,
        lf: Lifecycle
    ) : FragmentStateAdapter(fm, lf) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val fragment = RankingFragment()
            val bundle = Bundle()
            when (position) {
                0 -> bundle.putString("rank_type", "all")
                1 -> bundle.putString("rank_type", "bypopularity")
                2 -> bundle.putString("rank_type", "favorite")
            }
            bundle.putInt("type", 1)
            fragment.arguments = bundle
            return fragment
        }
    }
}