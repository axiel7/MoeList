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
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.MyAnimeListAdapter
import com.axiel7.moelist.adapter.PlusButtonTouchedListener
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.model.UserAnimeList
import com.axiel7.moelist.model.UserAnimeListResponse
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.MainActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AnimeListFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var animeListRecycler: RecyclerView
    private lateinit var filtersFab: FloatingActionButton
    private lateinit var loadingBar: ContentLoadingProgressBar
    private lateinit var snackBarView: View
    private lateinit var animeListAdapter: MyAnimeListAdapter
    private var animeListResponse: UserAnimeListResponse? = null
    private lateinit var animeList: MutableList<UserAnimeList>
    private lateinit var malApiService: MalApiService
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var listStatus: String
    private lateinit var sortMode: String
    private var defaultStatus: Int? = null
    private var defaultSort: Int = 0
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

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

        createRetrofitAndApiService()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_animelist, container, false)
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackBarView = view
        loadingBar = view.findViewById(R.id.loading_animelist)
        loadingBar.hide()
        if (animeList.isEmpty()) {
            loadingBar.show()
        }

        animeListRecycler = view.findViewById(R.id.animelist_recycler)
        animeListAdapter =
                MyAnimeListAdapter(
                    animeList,
                    R.layout.list_item_animelist,
                    requireContext(),
                    onClickListener = { itemView, userAnimeList -> openDetails(userAnimeList.node.id, itemView) }
                )
        animeListAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (animeListResponse!=null) {
                    val nextPage: String? = animeListResponse!!.paging.next
                    if (nextPage!=null) {
                        val getMoreCall = malApiService.getNextAnimeListPage(nextPage)
                        initAnimeListCall(getMoreCall, false)
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
                        addOneEpisode(animeId, watchedEpisodes.plus(1), "completed")
                    }
                    else {
                        addOneEpisode(animeId, watchedEpisodes?.plus(1), null)
                    }
                }
                val manager = ReviewManagerFactory.create(requireContext())
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { requestReview ->
                    if (requestReview.isSuccessful) {
                        // We got the ReviewInfo object
                        val reviewInfo = requestReview.result
                        val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                    }
                }
            }
        })

        animeListRecycler.adapter = animeListAdapter

        // filters dialog
        filtersFab = view.findViewById(R.id.filters_fab)
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filters, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)
        filtersFab.setOnClickListener { bottomSheetDialog.show() }

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
            initCalls()
        }

        val sortView = dialogView.findViewById<LinearLayoutCompat>(R.id.sort)
        val sortSummary = sortView.findViewById<TextView>(R.id.sort_mode)
        sortSummary.text = StringFormat.formatSortOption(sortMode, requireContext())
        sortView?.setOnClickListener {
            val items = arrayOf(requireContext().getString(R.string.sort_title),
                requireContext().getString(R.string.sort_score),
                requireContext().getString(R.string.sort_last_updated))
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sort")
                .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                    // Respond to neutral button press
                }
                .setPositiveButton("OK") { _, _ ->
                    // Respond to positive button press
                    sharedPref.saveInt("sortAnime", defaultSort)
                    sortSummary.text = StringFormat.formatSortOption(sortMode, requireContext())
                    initCalls()
                }
                // Single-choice items (initialized with checked item)
                .setSingleChoiceItems(items, defaultSort) { _, which ->
                    // Respond to item chosen
                    defaultSort = which
                    changeSortFilter(which)
                }
                .show()
        }

        animeListRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    //scroll down
                    filtersFab.hide()
                } else if (dy < 0) {
                    //scroll up
                    filtersFab.show()
                }
            }
        })

        initCalls()
    }
    private fun createRetrofitAndApiService() {
        retrofit = if (MainActivity.httpClient!=null) {
            Retrofit.Builder()
                .baseUrl(Urls.apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(MainActivity.httpClient!!)
                .build()
        } else {
            Retrofit.Builder()
                .baseUrl(Urls.apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(CreateOkHttpClient.createOkHttpClient(requireContext(), true))
                .build()
        }

        malApiService = retrofit?.create(MalApiService::class.java)!!
    }
    private fun initCalls() {
        val animeListCall = malApiService.getUserAnimeList(listStatus, "list_status,num_episodes,media_type,status", sortMode)
        loadingBar.show()
        initAnimeListCall(animeListCall, true)
    }
    private fun initAnimeListCall(call: Call<UserAnimeListResponse>, shouldClear: Boolean) {
        call.enqueue(object: Callback<UserAnimeListResponse> {
            override fun onResponse(call: Call<UserAnimeListResponse>, response: Response<UserAnimeListResponse>) {
                //Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    //sharedPref.deleteValue("animeListResponse$listStatus")
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
                        animeList.addAll(animeList2)
                        loadingBar.hide()
                        animeDb?.userAnimeListDao()?.insertUserAnimeList(animeList)
                        animeListAdapter.notifyDataSetChanged()
                    }
                    else {
                        animeListResponse = responseOld
                    }
                }
                //TODO(not tested)
                else if (response.code()==401) {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    sharedPref.saveString("accessToken", accessToken)
                    sharedPref.saveString("refreshToken", refreshToken)

                    call.clone()
                }
            }

            override fun onFailure(call: Call<UserAnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

        })
    }
    private fun addOneEpisode(animeId: Int, watchedEpisodes: Int?, status: String?) {
        loadingBar.show()
        val shouldNotUpdate = watchedEpisodes==null
        if (!shouldNotUpdate) {
            val updateListCall = malApiService
                .updateAnimeList(Urls.apiBaseUrl + "anime/$animeId/my_list_status", status, null, watchedEpisodes)
            updateListCall.enqueue(object :Callback<MyListStatus> {
                override fun onResponse(call: Call<MyListStatus>, response: Response<MyListStatus>) {
                    if (response.isSuccessful) {
                        //val myListStatus = response.body()!!
                        initCalls()
                        if (isAdded) {
                            Snackbar.make(snackBarView, getString(R.string.updated), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    else if (isAdded) {
                        Snackbar.make(snackBarView, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                    Log.d("MoeLog", t.toString())
                    if (isAdded) {
                        Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            loadingBar.hide()
            if (isAdded) {
                Snackbar.make(snackBarView, getString(R.string.no_changes), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun changeStatusFilter(radioButton: Int) {
        listStatus = when(radioButton) {
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
    private fun openDetails(animeId: Int?, view: View?) {
        if (view!=null) {
            val poster = view.findViewById<FrameLayout>(R.id.poster_container)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.transitionName)
            val intent = Intent(context, AnimeDetailsActivity::class.java)
            intent.putExtra("animeId", animeId)
            startActivityForResult(intent, 17, bundle.toBundle())
        }
        else {
            val intent = Intent(context, AnimeDetailsActivity::class.java)
            intent.putExtra("animeId", animeId)
            startActivityForResult(intent, 17)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==17 && resultCode==Activity.RESULT_OK) {
            val shouldUpdate :Boolean = data?.extras?.get("entryUpdated") as Boolean
            if (shouldUpdate) {
                initCalls()
            }
        }
    }
}