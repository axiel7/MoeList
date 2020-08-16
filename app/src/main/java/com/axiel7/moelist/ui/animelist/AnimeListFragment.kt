package com.axiel7.moelist.ui.animelist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
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
import com.axiel7.moelist.utils.CreateOkHttpClient
import com.axiel7.moelist.utils.RefreshToken
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var animeListAdapter: MyAnimeListAdapter
    private var animeListResponse: UserAnimeListResponse? = null
    private lateinit var animeList: MutableList<UserAnimeList>
    private lateinit var malApiService: MalApiService
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var listStatus: String
    private var defaultStatus: Int? = null
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        defaultStatus = sharedPref.getInt("checkedStatusButton", R.id.watching_button)
        if (defaultStatus==null) {
            defaultStatus = R.id.watching_button
        }
        changeStatusFilter(defaultStatus!!)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    onClickListener = { _, userAnimeList -> openDetails(userAnimeList.node.id) }
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
            }
        })

        animeListRecycler.adapter = animeListAdapter


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
            if (animeDb?.userAnimeListDao()?.getUserAnimeListByStatus(listStatus)!=null) {
                animeList.clear()
                val animeList2 = animeDb?.userAnimeListDao()?.getUserAnimeListByStatus(listStatus)!!
                animeList.addAll(animeList2)
                animeListAdapter.notifyDataSetChanged()
            }
            initCalls()
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
        val animeListCall = malApiService.getUserAnimeList(listStatus, "list_status,num_episodes,media_type,status", "anime_title")
        initAnimeListCall(animeListCall, true)
    }
    private fun initAnimeListCall(call: Call<UserAnimeListResponse>, shouldClear: Boolean) {
        call.enqueue(object: Callback<UserAnimeListResponse> {
            override fun onResponse(call: Call<UserAnimeListResponse>, response: Response<UserAnimeListResponse>) {
                //Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    animeListResponse = response.body()!!
                    val animeList2 = animeListResponse!!.data
                    if (animeList2!=animeList) {
                        for (anime in animeList2) {
                            anime.status = listStatus
                        }
                        if (shouldClear) {
                            animeList.clear()
                        }
                        animeList.addAll(animeList2)
                        loadingBar.hide()
                        animeDb?.userAnimeListDao()?.insertUserAnimeList(animeList)
                        animeListAdapter.notifyDataSetChanged()
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
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(context, "Error updating list", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                    Log.d("MoeLog", t.toString())
                    Toast.makeText(context, "Error updating list", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            loadingBar.hide()
            Toast.makeText(context, "No changes", Toast.LENGTH_SHORT).show()
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
    private fun openDetails(animeId: Int?) {
        val intent = Intent(context, AnimeDetailsActivity::class.java)
        intent.putExtra("animeId", animeId)
        startActivityForResult(intent, 17)
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