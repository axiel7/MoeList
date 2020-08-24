package com.axiel7.moelist.ui.charts

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.axiel7.moelist.R
import com.google.android.material.tabs.TabLayout

class RankingActivity : AppCompatActivity() {

    private val rankingAllFragment = RankingFragment()
    private val rankingPopFragment = RankingFragment()
    private val allBundle = Bundle()
    private val popBundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorCard)

        val mediaType = intent.extras?.getString("mediaType", "anime").toString()
        allBundle.putString("mediaType", mediaType)
        allBundle.putString("rankType", "all")
        popBundle.putString("mediaType", mediaType)
        popBundle.putString("rankType", "bypopularity")
        rankingAllFragment.arguments = allBundle
        rankingPopFragment.arguments = popBundle

        val viewPager = findViewById<ViewPager>(R.id.ranking_viewpager)
        viewPager.offscreenPageLimit = 0
        setupViewPager(viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.ranking_tab_layout)
        tabLayout.setupWithViewPager(viewPager)

        val toolbar = findViewById<Toolbar>(R.id.ranking_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        when(mediaType) {
            "anime" -> toolbar.title = getString(R.string.anime_ranking)
            "manga" -> toolbar.title = getString(R.string.manga)
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

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitles[position]
        }

    }
}