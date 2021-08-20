package com.axiel7.moelist.ui.seasonal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.paging.LoadState
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.SeasonalAnimeAdapter
import com.axiel7.moelist.databinding.FragmentSeasonalBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Extensions.toInt
import com.axiel7.moelist.utils.StringExtensions.formatSeason
import kotlinx.coroutines.flow.collectLatest

class SeasonalFragment : BaseFragment<FragmentSeasonalBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSeasonalBinding
        get() = FragmentSeasonalBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: SeasonalViewModel by activityViewModels()
    private lateinit var adapter: SeasonalAnimeAdapter

    override fun setup() {
        viewModel.setNsfw(sharedPref.getBoolean("nsfw", false).toInt())

        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        binding.fabFilter.setOnClickListener {
            SeasonalFilterFragment().show(childFragmentManager, "filters")
        }

        adapter = SeasonalAnimeAdapter(safeContext,
            onClick = { _, item ->
                mainViewModel.selectId(item.node.id)
                mainActivity?.navigate(
                    idAction = R.id.action_seasonalFragment_to_animeDetailsFragment
                )
            }
        )
        binding.listSeasonal.adapter = adapter

        launchLifecycleStarted {
            adapter.loadStateFlow.collectLatest {
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
            viewModel.animeSeasonalFlow.collectLatest {
                adapter.submitData(it)
            }
        }

        val season = viewModel.startSeason.value.season.formatSeason(safeContext)
        val year = viewModel.startSeason.value.year
        binding.toolbar.title = "$season $year"
    }

    fun changeSeason(year: Int, season: String) {
        val seasonFormatted = season.formatSeason(safeContext)
        binding.toolbar.title = "$seasonFormatted $year"
        viewModel.setStartSeason(year, season)
        viewModel.updateAnimeSeasonalFlow()
        onViewCreated(binding.root, null)
    }
}