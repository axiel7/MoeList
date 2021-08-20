package com.axiel7.moelist.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearSnapHelper
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AiringAnimeAdapter
import com.axiel7.moelist.adapter.CurrentSeasonalAdapter
import com.axiel7.moelist.adapter.RecommendationsAdapter
import com.axiel7.moelist.data.model.Season
import com.axiel7.moelist.databinding.FragmentHomeBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.RESPONSE_OK
import com.axiel7.moelist.utils.Extensions.setDrawables
import com.axiel7.moelist.utils.Extensions.toInt
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.flow.collectLatest

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapterToday: AiringAnimeAdapter
    private lateinit var adapterSeason: CurrentSeasonalAdapter
    private lateinit var adapterRecommend: RecommendationsAdapter

    override fun setup() {
        viewModel.setNsfw(sharedPref.getBoolean("nsfw", false).toInt())

        initUI()
        collectData()
    }

    private fun initUI() {
        binding.animeRank.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_navigation_home_to_animeRankingFragment
            )
        }

        binding.mangaRank.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_navigation_home_to_mangaRankingFragment
            )
        }

        val seasonIcon = when (SeasonCalendar.currentSeason) {
            Season.WINTER -> R.drawable.ic_winter_24
            Season.SPRING -> R.drawable.ic_spring_24
            Season.SUMMER -> R.drawable.ic_summer_24
            Season.FALL -> R.drawable.ic_fall_24
        }
        binding.seasonalText.setDrawables(start = seasonIcon)
        binding.seasonalChart.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_navigation_home_to_seasonalFragment
            )
        }

        binding.calendar.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_navigation_home_to_hostCalendarFragment
            )
        }

        LinearSnapHelper().apply {
            attachToRecyclerView(binding.todayList)
        }
        adapterToday = AiringAnimeAdapter(safeContext,
            onClick = { _, item ->
                mainViewModel.selectId(item.node.id)
                mainActivity?.navigate(
                    idAction = R.id.action_navigation_home_to_animeDetailsFragment
                )
            }
        )
        binding.todayList.adapter = adapterToday

        binding.todayTitle.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_navigation_home_to_hostCalendarFragment
            )
        }

        adapterSeason = CurrentSeasonalAdapter(
            onClick = { itemView, item ->
                mainViewModel.selectId(item.node.id)
                val poster = itemView.findViewById<ImageView>(R.id.poster)
                poster.transitionName = "shared_poster_container"
                mainActivity?.navigate(
                    idAction = R.id.action_navigation_home_to_animeDetailsFragment
                )
            }
        )
        binding.seasonList.adapter = adapterSeason

        adapterRecommend = RecommendationsAdapter(
            onClick = { _, item ->
                mainViewModel.selectId(item.node.id)
                mainActivity?.navigate(
                    idAction = R.id.action_navigation_home_to_animeDetailsFragment
                )
            }
        )
        binding.recommendList.adapter = adapterRecommend
    }

    private fun collectData() {
        launchLifecycleStarted {
            viewModel.todayResponse.collectLatest {
                when (it.second) {
                    RESPONSE_OK -> {
                        adapterToday.setData(it.first)
                        binding.loadingToday.hide()
                        if (it.first.isEmpty()) {
                            binding.emptyToday.visibility = View.VISIBLE
                        } else {
                            binding.emptyToday.visibility = View.INVISIBLE
                        }
                    }
                    RESPONSE_ERROR -> {
                        binding.loadingToday.hide()
                        if (it.first.isEmpty()) {
                            binding.emptyToday.visibility = View.VISIBLE
                        } else {
                            binding.emptyToday.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

        launchLifecycleStarted {
            adapterSeason.loadStateFlow.collectLatest {
                if (it.refresh is LoadState.Loading) binding.loadingSeason.show()
                else binding.loadingSeason.hide()
                if (it.refresh is LoadState.Error) {
                    if (adapterSeason.itemCount <= 0) {
                        binding.emptySeason.visibility = View.VISIBLE
                    } else {
                        binding.emptySeason.visibility = View.INVISIBLE
                    }
                }
            }
        }

        launchLifecycleStarted {
            viewModel.animeSeasonalFlow.collectLatest {
                adapterSeason.submitData(it)
            }
        }

        launchLifecycleStarted {
            adapterRecommend.loadStateFlow.collectLatest {
                if (it.refresh is LoadState.Loading) binding.loadingRecommend.show()
                else binding.loadingRecommend.hide()
                if (it.refresh is LoadState.Error) {
                    if (adapterRecommend.itemCount <= 0) {
                        binding.emptyRecommend.visibility = View.VISIBLE
                    } else {
                        binding.emptyRecommend.visibility = View.INVISIBLE
                    }
                }
            }
        }

        launchLifecycleStarted {
            viewModel.animeRecommendFlow.collectLatest {
                adapterRecommend.submitData(it)
            }
        }
    }

}