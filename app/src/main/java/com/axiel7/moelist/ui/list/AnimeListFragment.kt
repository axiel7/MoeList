package com.axiel7.moelist.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.HeaderAdapter
import com.axiel7.moelist.adapter.UserAnimeListAdapter
import com.axiel7.moelist.databinding.FragmentListBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.details.anime.EditAnimeFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.SORT_ANIME_TITLE
import com.axiel7.moelist.utils.Constants.STATUS_COMPLETED
import com.axiel7.moelist.utils.Constants.STATUS_WATCHING
import com.axiel7.moelist.utils.Extensions.toInt
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class AnimeListFragment : BaseFragment<FragmentListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentListBinding
        get() = FragmentListBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: AnimeListViewModel by viewModels()
    private lateinit var adapter: UserAnimeListAdapter
    private var status: String = STATUS_WATCHING
    private var defaultSort: String = SORT_ANIME_TITLE
    private val sortItems: Array<String> by lazy {
        arrayOf(getString(R.string.sort_title),
            getString(R.string.sort_score),
            getString(R.string.sort_last_updated))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = arguments?.getString("status") ?: STATUS_WATCHING
        defaultSort = sharedPref.getString("last_sort_anime", SORT_ANIME_TITLE) ?: SORT_ANIME_TITLE
    }

    override fun setup() {

        viewModel.setStatus(status)
        viewModel.setSortMode(defaultSort)
        viewModel.setNsfw(sharedPref.getBoolean("nsfw", false).toInt())

        adapter = UserAnimeListAdapter(
            safeContext,
            onClick = { _, item, _ ->
                mainViewModel.selectId(item.node.id)
                mainActivity?.navigate(
                    idAction = R.id.action_navigation_anime_list_to_animeDetailsFragment
                )
            },
            onLongClick = { _, item, _ ->
                EditAnimeFragment(
                    item.listStatus,
                    item.node.id,
                    item.node.numEpisodes ?: 0
                ).show(parentFragmentManager, "Edit")
            },
            onPlusButtonClick = { _, item, _ ->
                val animeId = item.node.id
                val watchedEpisodes = item.listStatus?.numEpisodesWatched
                item.node.numEpisodes?.let {
                    if (watchedEpisodes == it.minus(1)) {
                        viewModel.updateList(
                            animeId = animeId,
                            status = STATUS_COMPLETED,
                            watchedEpisodes = watchedEpisodes.plus(1))
                    }
                    else {
                        viewModel.updateList(
                            animeId = animeId,
                            watchedEpisodes = watchedEpisodes?.plus(1))
                    }
                }
            }
        )

        binding.loading.setOnRefreshListener { adapter.refresh() }

        launchLifecycleStarted {
            viewModel.animeListFlow.collectLatest {
                adapter.submitData(it)
            }
        }

        launchLifecycleStarted {
            adapter.loadStateFlow.collectLatest {
                binding.loading.isRefreshing = it.refresh is LoadState.Loading
                if (it.refresh is LoadState.Error) {
                    showSnackbar(getString(R.string.error_server))
                }
            }
        }

        launchLifecycleStarted {
            viewModel.updateResponse.collectLatest {
                if (it.first != null) {
                    if (!it.first!!.error.isNullOrEmpty() || !it.first!!.message.isNullOrEmpty()) {
                        showSnackbar("${it.first!!.error}: ${it.first!!.message}")
                    }
                    else {
                        adapter.refresh()
                    }
                }
                else if (it.second == RESPONSE_ERROR) {
                    showSnackbar(getString(R.string.error_updating_list))
                }
            }
        }

        val headerAdapter = HeaderAdapter(
            onClickSort = { showSortDialog() }
        )
        val concatAdapter = ConcatAdapter(headerAdapter, adapter)
        binding.list.adapter = concatAdapter

    }

    private fun changeSort(radioButtonPos: Int) {
        val sort = viewModel.getSortFromPosition(radioButtonPos)
        defaultSort = sort
        sharedPref.saveString("last_sort_anime", sort)
        viewModel.setSortMode(sort)
        viewModel.updateAnimeListFlow()
        onViewCreated(binding.root, null)
    }

    private fun showSortDialog() {
        var radioButtonPos = -1
        val lastSort = viewModel.getPositionFromSort(defaultSort)
        MaterialAlertDialogBuilder(safeContext)
            .setTitle(getString(R.string.sort))
            .setNeutralButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                changeSort(radioButtonPos)
            }
            .setSingleChoiceItems(sortItems, lastSort) { _, which ->
                radioButtonPos = which
            }
            .show()
    }

}