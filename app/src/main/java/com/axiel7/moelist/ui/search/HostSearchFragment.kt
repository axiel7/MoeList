package com.axiel7.moelist.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.FragmentHostSearchBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Extensions.hideKeyboard
import com.axiel7.moelist.utils.Extensions.showKeyboard
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.random.Random

class HostSearchFragment : BaseFragment<FragmentHostSearchBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHostSearchBinding
        get() = FragmentHostSearchBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val currentFragment
        get() = childFragmentManager.findFragmentByTag("f${binding.listViewPager.currentItem}")
    val searchQuery get() = binding.searchView.query.toString()

    override fun setup() {
        binding.searchToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        binding.searchToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.random -> {
                    mainViewModel.selectId(Random.nextInt(from = 0, until = 5000))
                    mainActivity?.navigate(
                        idAction = if (Random.nextBoolean()) R.id.action_hostSearchFragment_to_animeDetailsFragment
                        else R.id.action_hostSearchFragment_to_mangaDetailsFragment
                    )
                    true
                }
                else -> false
            }
        }

        binding.listViewPager.adapter = SearchPagerAdapter(childFragmentManager, lifecycle)

        TabLayoutMediator(binding.tabLayout, binding.listViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.anime)
                1 -> tab.text = getString(R.string.manga)
            }
        }.attach()

        binding.listViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.searchView.queryHint = getString(R.string.search_anime)
                    1 -> binding.searchView.queryHint = getString(R.string.search_manga)
                }
                (currentFragment as? SearchFragment)?.search(searchQuery)
            }
        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    (currentFragment as? SearchFragment)?.search(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) activity?.window?.showKeyboard()
            else activity?.window?.hideKeyboard()
        }
        binding.searchView.requestFocus()
    }

    inner class SearchPagerAdapter(
        fm: FragmentManager,
        lf: Lifecycle
    ) : FragmentStateAdapter(fm, lf) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val fragment = SearchFragment()
            val bundle = Bundle()
            when (position) {
                0 -> bundle.putInt("type", 0)
                1 -> bundle.putInt("type", 1)
                else -> bundle.putInt("type", 0)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}