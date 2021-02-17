package com.axiel7.moelist.ui.charts

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.BaseActivity
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.android.synthetic.main.activity_ranking.*

class RankingActivity : BaseActivity() {

    private val rankingAllFragment = RankingFragment()
    private val rankingPopFragment = RankingFragment()
    private val allBundle = Bundle()
    private val popBundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        window.statusBarColor = getColorFromAttr(R.attr.colorToolbar)

        val mediaType = intent.extras?.getString("mediaType", "anime").toString()
        allBundle.putString("mediaType", mediaType)
        allBundle.putString("rankType", "all")
        popBundle.putString("mediaType", mediaType)
        popBundle.putString("rankType", "bypopularity")
        rankingAllFragment.arguments = allBundle
        rankingPopFragment.arguments = popBundle

        ranking_viewpager.offscreenPageLimit = 0
        setupViewPager(ranking_viewpager)
        ranking_tab_layout.setupWithViewPager(ranking_viewpager)

        setSupportActionBar(ranking_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        ranking_toolbar.setNavigationOnClickListener { onBackPressed() }
        when(mediaType) {
            "anime" -> ranking_toolbar.title = getString(R.string.anime_ranking)
            "manga" -> ranking_toolbar.title = getString(R.string.manga)
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