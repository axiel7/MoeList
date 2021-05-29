package com.axiel7.moelist.ui.animelist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.UseCases
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.MyAnimeListAdapter
import com.axiel7.moelist.adapter.PlusButtonTouchedListener
import com.axiel7.moelist.databinding.FragmentAnimelistBinding
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.model.UserAnimeList
import com.axiel7.moelist.model.UserAnimeListResponse
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.ui.details.EditAnimeFragment
import com.axiel7.moelist.utils.ResponseConverter
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AnimeListFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var animeListAdapter: MyAnimeListAdapter
    private var animeListResponse: UserAnimeListResponse? = null
    private lateinit var animeList: MutableList<UserAnimeList>
    private lateinit var listStatus: String
    private lateinit var sortMode: String
    private var defaultStatus: Int? = null
    private var defaultSort: Int = 0
    private var showNsfw = 0
    private var _binding: FragmentAnimelistBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = SharedPrefsHelpers.instance!!

        showNsfw = if (sharedPref.getBoolean("nsfw", false)) { 1 } else { 0 }
        defaultStatus = sharedPref.getInt("checkedStatusButton", R.id.watching_button)
        if (defaultStatus==null || defaultStatus==0) {
            defaultStatus = R.id.watching_button
        }
        changeStatusFilter(defaultStatus!!)
        defaultSort = sharedPref.getInt("sortAnime", 0)
        changeSortFilter(defaultSort)

        if (animeDb?.userAnimeListDao()?.getUserAnimeListByStatus(listStatus)!=null) {
            animeList = animeDb?.userAnimeListDao()?.getUserAnimeListByStatus(listStatus)!!
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimelistBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            binding.loadingAnimelist.isRefreshing = false
            if (animeList.isEmpty()) {
                binding.loadingAnimelist.isRefreshing = true
            }
        }

        binding.loadingAnimelist.setOnRefreshListener { initCalls(true, null) }

        animeListAdapter =
                MyAnimeListAdapter(
                    animeList,
                    requireContext(),
                    onClickListener = { itemView, userAnimeList, pos ->
                        openDetails(userAnimeList.node.id, itemView, pos) },
                    onLongClickListener = { _, userAnimeList, pos ->
                        val editSheet =
                            EditAnimeFragment(userAnimeList.list_status,
                                userAnimeList.node.id,
                                userAnimeList.node.num_episodes ?: 0, pos)
                        editSheet.show(parentFragmentManager, "Edit")
                    }
                )
        animeListAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int, lastPosition: Int) {
                if (animeListResponse!=null) {
                    val nextPage: String? = animeListResponse?.paging?.next
                    if (nextPage!=null) {
                        val getMoreCall = malApiService.getNextAnimeListPage(nextPage)
                        initAnimeListCall(getMoreCall, false, null, lastPosition)
                    }
                }
            }
        })
        animeListAdapter.setPlusButtonTouchedListener(object :PlusButtonTouchedListener {
            override fun onButtonTouched(view: View, position: Int) {
                val animeId = animeListAdapter.getAnimeId(position)
                val watchedEpisodes = animeListAdapter.getWatchedEpisodes(position)
                val totalEpisodes = animeListAdapter.getTotalEpisodes(position)
                if (totalEpisodes!=null) {
                    if (watchedEpisodes==totalEpisodes.minus(1)) {
                        addOneEpisode(animeId, watchedEpisodes.plus(1), "completed", position)
                    }
                    else {
                        addOneEpisode(animeId, watchedEpisodes?.plus(1), null, position)
                    }
                }

                //Google Play review prompt
                val manager = ReviewManagerFactory.create(requireContext())
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { requestReview ->
                    if (requestReview.isSuccessful) {
                        // We got the ReviewInfo object
                        val reviewInfo = requestReview.result
                        manager.launchReviewFlow(requireActivity(), reviewInfo)
                    }
                }
            }
        })

        binding.animelistRecycler.adapter = animeListAdapter

        // filters dialog
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filters, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)
        binding.filtersFab.setOnClickListener { bottomSheetDialog.show() }

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.status_radio_group)
        val defaultCheck = defaultStatus!!
        radioGroup.check(defaultCheck)
        changeStatusFilter(defaultCheck)
        radioGroup.setOnCheckedChangeListener{ _, i ->
            changeStatusFilter(i)
            sharedPref.saveInt("checkedStatusButton", i)
            animeList.clear()
            if (animeDb?.userAnimeListDao()?.getUserAnimeListByStatus(listStatus)!=null) {
                val animeList2 = animeDb?.userAnimeListDao()?.getUserAnimeListByStatus(listStatus)!!
                animeList.addAll(animeList2)
                animeListAdapter.notifyDataSetChanged()
            }
            bottomSheetDialog.dismiss()
            initCalls(true, null)
        }

        val sortView = dialogView.findViewById<LinearLayoutCompat>(R.id.sort)
        val sortSummary = sortView.findViewById<TextView>(R.id.sort_mode)
        sortSummary.text = StringFormat.formatSortOption(sortMode, requireContext())
        sortView?.setOnClickListener {
            val items = arrayOf(requireContext().getString(R.string.sort_title),
                requireContext().getString(R.string.sort_score),
                requireContext().getString(R.string.sort_last_updated))
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.sort))
                .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                    // Respond to neutral button press
                }
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    // Respond to positive button press
                    sharedPref.saveInt("sortAnime", defaultSort)
                    sortSummary.text = StringFormat.formatSortOption(sortMode, requireContext())
                    bottomSheetDialog.dismiss()
                    initCalls(true, null)
                }
                // Single-choice items (initialized with checked item)
                .setSingleChoiceItems(items, defaultSort) { _, which ->
                    // Respond to item chosen
                    defaultSort = which
                    changeSortFilter(which)
                }
                .show()
        }

        binding.animelistRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    //scroll down
                    binding.filtersFab.hide()
                } else if (dy < 0) {
                    //scroll up
                    binding.filtersFab.show()
                }
            }
        })

        initCalls(true, null)
    }
    private fun initCalls(shouldClear: Boolean, position: Int?) {
        val animeListCall = if (listStatus == "all") {
            // To return all anime, don't specify status field.
            malApiService.getUserAnimeList(null, "list_status,num_episodes,media_type,status", sortMode, showNsfw)
        } else {
            malApiService.getUserAnimeList(listStatus, "list_status,num_episodes,media_type,status", sortMode, showNsfw)
        }
        if (isAdded) {
            binding.loadingAnimelist.isRefreshing = true
            initAnimeListCall(animeListCall, shouldClear, position, null)
        }
    }
    private fun initAnimeListCall(call: Call<UserAnimeListResponse>, shouldClear: Boolean, position: Int?, lastPosition: Int?) {
        call.enqueue(object: Callback<UserAnimeListResponse> {
            override fun onResponse(call: Call<UserAnimeListResponse>, response: Response<UserAnimeListResponse>) {

                if (response.isSuccessful && isAdded) {
                    val responseOld = ResponseConverter
                        .stringToUserAnimeListResponse(sharedPref.getString("animeListResponse$listStatus", ""))
                    if (responseOld!=response.body() || animeList.isEmpty()) {
                        animeListResponse = response.body()

                        val animeList2 = animeListResponse!!.data
                        for (anime in animeList2) {
                            anime.status = anime.list_status?.status
                        }
                        if (shouldClear) {
                            sharedPref.saveString("animeListResponse$listStatus",
                                ResponseConverter.userAnimeListResponseToString(animeListResponse))
                            animeDb?.userAnimeListDao()?.deleteUserAnimeList(animeList)
                            animeList.clear()
                        }
                        when {
                            shouldClear -> {
                                animeList.addAll(animeList2)
                                animeListAdapter.notifyDataSetChanged()
                            }
                            lastPosition != null -> {
                                animeList.addAll(animeList2)
                                animeListAdapter.notifyItemRangeInserted(lastPosition, animeList2.size)
                            }
                            position != null -> {
                                animeList[position] = animeList2[position]
                                animeListAdapter.notifyItemChanged(position)
                            }
                        }
                        animeDb?.userAnimeListDao()?.insertUserAnimeList(animeList)
                        binding.loadingAnimelist.isRefreshing = false
                    }
                    else {
                        animeListResponse = responseOld
                    }
                }

                else if (response.code()==401) {
                    if (isAdded) {
                        UseCases.logOut(requireContext())
                    }
                }
            }

            override fun onFailure(call: Call<UserAnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    binding.loadingAnimelist.isRefreshing = false
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

        })
    }
    private fun addOneEpisode(animeId: Int, watchedEpisodes: Int?, status: String?, position: Int) {
        if (isAdded) {
            binding.loadingAnimelist.isRefreshing = true
        }
        val shouldNotUpdate = watchedEpisodes==null
        if (!shouldNotUpdate) {
            val updateListCall = malApiService
                .updateAnimeList(Urls.apiBaseUrl + "anime/$animeId/my_list_status", status, null, watchedEpisodes)
            updateListCall.enqueue(object :Callback<MyListStatus> {
                override fun onResponse(call: Call<MyListStatus>, response: Response<MyListStatus>) {
                    if (response.isSuccessful && isAdded) {
                        initCalls(false, position)
                    }
                    else if (isAdded) {
                        Snackbar.make(binding.root, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                    Log.d("MoeLog", t.toString())
                    if (isAdded) {
                        Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            if (isAdded) {
                binding.loadingAnimelist.isRefreshing = false
                Snackbar.make(binding.root, getString(R.string.no_changes), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun changeStatusFilter(radioButton: Int) {
        listStatus = when(radioButton) {
            R.id.all_button -> "all"
            R.id.watching_button -> "watching"
            R.id.completed_button -> "completed"
            R.id.onhold_button -> "on_hold"
            R.id.dropped_button -> "dropped"
            R.id.ptw_button -> "plan_to_watch"
            else -> "watching"
        }
    }
    private fun changeSortFilter(radioButton: Int) {
        sortMode = when(radioButton) {
            0 -> "anime_title"
            1 -> "list_score"
            2 -> "list_updated_at"
            else -> "anime_title"
        }
    }
    private fun openDetails(animeId: Int?, view: View?, position: Int) {
        if (view!=null) {
            val poster = view.findViewById<FrameLayout>(R.id.poster_container)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.transitionName)
            val intent = Intent(context, AnimeDetailsActivity::class.java)
            intent.putExtra("animeId", animeId)
            intent.putExtra("position", position)
            startActivityForResult(intent, 17, bundle.toBundle())
        }
        else {
            val intent = Intent(context, AnimeDetailsActivity::class.java)
            intent.putExtra("animeId", animeId)
            intent.putExtra("position", position)
            startActivityForResult(intent, 17)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==17 && resultCode==Activity.RESULT_OK) {
            val shouldUpdate: Boolean = data?.extras?.getBoolean("entryUpdated", false) ?: false
            val position: Int? = data?.extras?.getInt("position")
            if (shouldUpdate) {
                initCalls(false, position)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}