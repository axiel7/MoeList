package com.axiel7.moelist.ui.charts

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityRankingBinding
import com.axiel7.moelist.ui.base.BaseActivity
import com.google.android.material.transition.platform.MaterialSharedAxis

class RankingActivity : BaseActivity<ActivityRankingBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityRankingBinding
        get() = ActivityRankingBinding::inflate
    private val rankingAllFragment = RankingFragment()
    private val rankingPopFragment = RankingFragment()
    private val allBundle = Bundle()
    private val popBundle = Bundle()

    override fun preCreate() {
        super.preCreate()
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
    }

    override fun setup() {
        window.statusBarColor = getColorFromAttr(R.attr.colorToolbar)

        val mediaType = intent.extras?.getString("mediaType", "anime").toString()
        allBundle.putString("mediaType", mediaType)
        allBundle.putString("rankType", "all")
        popBundle.putString("mediaType", mediaType)
        popBundle.putString("rankType", "bypopularity")
        rankingAllFragment.arguments = allBundle
        rankingPopFragment.arguments = popBundle

        binding.rankingViewpager.offscreenPageLimit = 0
        setupViewPager(binding.rankingViewpager)
        binding.rankingTabLayout.setupWithViewPager(binding.rankingViewpager)

        setSupportActionBar(binding.rankingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.rankingToolbar.setNavigationOnClickListener { onBackPressed() }
        when(mediaType) {
            "anime" -> binding.rankingToolbar.title = getString(R.string.anime_ranking)
            "manga" -> binding.rankingToolbar.title = getString(R.string.manga)
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = RankingPagerAdapter(supportFragmentManager)
        adapter.addFragment(rankingAllFragment, getString(R.string.all))
        adapter.addFragment(rankingPopFragment, getString(R.string.popular))
        viewPager.adapter = adapter
    }

    internal class RankingPagerAdapter(manager: FragmentManager?) :
        FragmentPagerAdapter(manager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragmentList = mutableListOf<Fragment>()
        private val fragmentTitles = mutableListOf<String>()

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitles.add(title)
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitles[position]
        }

    }
}