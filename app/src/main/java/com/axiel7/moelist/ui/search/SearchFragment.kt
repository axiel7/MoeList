package com.axiel7.moelist.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.SearchAnimeAdapter
import com.axiel7.moelist.adapter.SearchMangaAdapter
import com.axiel7.moelist.databinding.FragmentSearchBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Extensions.toInt
import kotlinx.coroutines.flow.collectLatest

class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchBinding
        get() = FragmentSearchBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAnimeAdapter: SearchAnimeAdapter
    private lateinit var searchMangaAdapter: SearchMangaAdapter
    private val searchType: Int by lazy {
        arguments?.getInt("type") ?: 0
    }

    override fun onResume() {
        super.onResume()
        binding.loading.hide()
    }

    override fun setup() {
        viewModel.setNsfw(sharedPref.getBoolean("nsfw", false).toInt())
        when (searchType) {
            0 -> initAnimeSearch()
            1 -> initMangaSearch()
        }

    }

    private fun initAnimeSearch() {
        searchAnimeAdapter = SearchAnimeAdapter(safeContext,
            onClick = { view, item, _ ->
                mainViewModel.selectId(item.node.id)
                mainActivity?.navigate(
                    R.id.action_hostSearchFragment_to_animeDetailsFragment,
                    //sharedView = poster
                )
            }
        )
        binding.listSearch.adapter = searchAnimeAdapter

        launchLifecycleStarted {
            searchAnimeAdapter.loadStateFlow.collectLatest {
                if (it.refresh is LoadState.Loading) {
                    binding.loading.show()
                } else {
                    binding.loading.hide()
                }
                if (it.refresh is LoadState.Error) {
                    showSnackbar(getString(R.string.error_server))
                }
            }
        }

        launchLifecycleStarted {
            viewModel.query.collectLatest { q ->
                if (q.isNotBlank()) {
                    launchLifecycleStarted {
                        viewModel.animeListFlow.collectLatest {
                            searchAnimeAdapter.submitData(it)
                        }
                    }
                }
            }
        }
    }

    private fun initMangaSearch() {
        searchMangaAdapter = SearchMangaAdapter(
            safeContext,
            onClick = { view, item, _ ->
                mainViewModel.selectId(item.node.id)
                mainActivity?.navigate(
                    R.id.action_hostSearchFragment_to_mangaDetailsFragment,
                )
            }
        )
        binding.listSearch.adapter = searchMangaAdapter

        launchLifecycleStarted {
            searchMangaAdapter.loadStateFlow.collectLatest {
                if (it.refresh is LoadState.Loading) {
                    binding.loading.show()
                } else {
                    binding.loading.hide()
                }
                if (it.refresh is LoadState.Error) {
                    showSnackbar(getString(R.string.error_server))
                }
            }
        }

        launchLifecycleStarted {
            viewModel.query.collectLatest { q ->
                if (q.isNotBlank()) {
                    launchLifecycleStarted {
                        viewModel.mangaListFlow.collectLatest {
                            searchMangaAdapter.submitData(it)
                        }
                    }
                }
            }
        }
    }

    fun search(query: String) {
        if (query.isNotBlank() && query != viewModel.query.value) {
            viewModel.setQuery(query)
            when (searchType) {
                0 -> searchAnimeAdapter.refresh()
                1 -> searchMangaAdapter.refresh()
            }
        }
    }

}