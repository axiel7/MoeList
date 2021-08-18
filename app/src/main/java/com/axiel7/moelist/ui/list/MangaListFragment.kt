package com.axiel7.moelist.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.HeaderAdapter
import com.axiel7.moelist.adapter.UserMangaListAdapter
import com.axiel7.moelist.databinding.FragmentListBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.details.manga.EditMangaFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Constants.SORT_MANGA_TITLE
import com.axiel7.moelist.utils.Constants.STATUS_COMPLETED
import com.axiel7.moelist.utils.Constants.STATUS_READING
import com.axiel7.moelist.utils.Extensions.toInt
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

class MangaListFragment : BaseFragment<FragmentListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentListBinding
        get() = FragmentListBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: MangaListViewModel by viewModels()
    private lateinit var adapter: UserMangaListAdapter
    private var status: String = STATUS_READING
    private var defaultSort: String = SORT_MANGA_TITLE
    private val sortItems: Array<String> by lazy {
        arrayOf(getString(R.string.sort_title),
            getString(R.string.sort_score),
            getString(R.string.sort_last_updated))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = arguments?.getString("status") ?: STATUS_READING
        defaultSort = sharedPref.getString("last_sort_manga", SORT_MANGA_TITLE) ?: SORT_MANGA_TITLE
    }

    @ExperimentalCoroutinesApi
    override fun setup() {

        viewModel.setStatus(status)
        viewModel.setSortMode(defaultSort)
        viewModel.setNsfw(sharedPref.getBoolean("nsfw", false).toInt())

        binding.list.updatePadding(
            bottom = binding.list.paddingBottom + (mainActivity?.bottomNavHeight ?: 0)
        )

        adapter = UserMangaListAdapter(
            safeContext,
            onClick = { _, item, _ ->
                mainViewModel.selectId(item.node.id)
                mainActivity?.navigate(
                    idAction = R.id.action_navigation_manga_list_to_mangaDetailsFragment
                )
            },
            onLongClick = { _, item, _ ->
                EditMangaFragment(
                    item.listStatus,
                    item.node.id,
                    item.node.numChapters ?: 0,
                    item.node.numVolumes ?: 0
                ).show(parentFragmentManager, "Edit")
            },
            onPlusButtonClick = { _, item, _ ->
                val mangaId = item.node.id
                val chaptersRead = item.listStatus?.numChaptersRead
                item.node.numChapters?.let {
                    if (chaptersRead == it.minus(1)) {
                        viewModel.updateList(
                            mangaId = mangaId,
                            status = STATUS_COMPLETED,
                            chaptersRead = chaptersRead.plus(1))
                    }
                    else {
                        viewModel.updateList(
                            mangaId = mangaId,
                            chaptersRead = chaptersRead?.plus(1))
                    }
                }
            }
        )

        binding.loading.setOnRefreshListener { adapter.refresh() }

        launchLifecycleStarted {
            viewModel.mangaListFlow.collectLatest {
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
        sharedPref.saveString("last_sort_manga", defaultSort)
        viewModel.setSortMode(sort)
        viewModel.updateMangaListFlow()
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