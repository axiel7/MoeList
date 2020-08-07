package com.axiel7.moelist.ui.animelist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.MyAnimeListAdapter
import com.axiel7.moelist.adapter.PlusButtonTouchedListener
import com.axiel7.moelist.model.UserAnimeList
import com.axiel7.moelist.model.UserAnimeListResponse
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.AnimeDetailsActivity
import com.axiel7.moelist.utils.CreateOkHttpClient.createOkHttpClient
import com.axiel7.moelist.utils.GetCacheFile
import com.axiel7.moelist.utils.RefreshToken
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Cache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AnimeListFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var animeListRecycler: RecyclerView
    private lateinit var filtersFab: FloatingActionButton
    private lateinit var animeListAdapter: MyAnimeListAdapter
    private lateinit var animeListResponse: UserAnimeListResponse
    private var savedAnimeListResponse: UserAnimeListResponse? = null
    private lateinit var animeList: MutableList<UserAnimeList>
    private lateinit var malApiService: MalApiService
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var listStatus: String
    private var retrofit: Retrofit? = null
    private var cache: Cache? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        animeList = mutableListOf()
        for (i in 0..5) {
            animeList.add(i, UserAnimeList(null, null))
        }

        savedAnimeListResponse = sharedPref.getObject("userAnimeListResponse", UserAnimeListResponse::class.java)

        if (savedAnimeListResponse!=null) {
            animeListResponse = savedAnimeListResponse as UserAnimeListResponse
            animeList = animeListResponse.data
        }

        listStatus = "watching"

        cache = context?.let { GetCacheFile.getCacheFile(it, 20) }

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_animelist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animeListRecycler = view.findViewById(R.id.animelist_recycler)
        animeListAdapter =
            context?.let {
                MyAnimeListAdapter(
                    animeList,
                    R.layout.list_item_animelist,
                    it,
                    onClickListener = { _, userAnimeList -> openDetails(userAnimeList.node?.id) }
                )
            }!!
        animeListAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int) {
                val nextPage: String? = animeListResponse.paging.next
                if (nextPage!=null) {
                    val getMoreCall = malApiService.getNextAnimeListPage(nextPage)
                    initAnimeListCall(getMoreCall, false)
                }
            }
        })
        animeListAdapter.setPlusButtonTouchedListener(object :PlusButtonTouchedListener {
            override fun onButtonTouched(position: Int) {
                TODO("Not yet implemented")
            }
        })

        animeListRecycler.adapter = animeListAdapter


        filtersFab = view.findViewById(R.id.filters_fab)
        val dialogView = layoutInflater.inflate(R.layout.anime_filters_sheet, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)
        val bottomSheetBehavior = bottomSheetDialog.behavior
        filtersFab.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.peekHeight = 1000
            bottomSheetDialog.show()
        }

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.status_radio_group)
        radioGroup.check(sharedPref.getInt("checkedStatusButton", R.id.watching_button))
        val checkedRadioButton = radioGroup.checkedRadioButtonId
        changeStatusFilter(checkedRadioButton)
        radioGroup.setOnCheckedChangeListener{ _, i ->
            changeStatusFilter(i)
            connectAndGetApiData()
            sharedPref.saveInt("checkedStatusButton", i)
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

        connectAndGetApiData()
    }
    private fun connectAndGetApiData() {
        if (retrofit==null) {
            retrofit = Retrofit.Builder()
                .baseUrl(Urls.apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient(accessToken, requireContext(), true))
                .build()
        }

        malApiService = retrofit?.create(MalApiService::class.java)!!
        val animeListCall = malApiService.getUserAnimeList(listStatus, "list_status,num_episodes,media_type,status", "anime_title")
        initAnimeListCall(animeListCall, true)
    }
    private fun initAnimeListCall(call: Call<UserAnimeListResponse>, shouldClear: Boolean) {
        call.enqueue(object: Callback<UserAnimeListResponse> {
            override fun onResponse(call: Call<UserAnimeListResponse>, response: Response<UserAnimeListResponse>) {
                Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    animeListResponse = response.body()!!
                    val animeList2 = animeListResponse.data

                    if (!shouldClear || savedAnimeListResponse != animeListResponse) {
                        if (shouldClear) {
                            animeList.clear()
                            sharedPref.saveObject("userAnimeListResponse", animeListResponse)
                        }
                        animeList.addAll(animeList2)

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
    private fun changeStatusFilter(radioButton: Int) {
        when(radioButton) {
            R.id.watching_button -> listStatus = "watching"
            R.id.completed_button -> listStatus = "completed"
            R.id.onhold_button -> listStatus = "on_hold"
            R.id.dropped_button -> listStatus = "dropped"
            R.id.ptw_button -> listStatus = "plan_to_watch"
        }
    }
    private fun openDetails(animeId: Int?) {
        Log.d("MoeLog", "item clicked")
        val intent = Intent(context, AnimeDetailsActivity::class.java)
        intent.putExtra("animeId", animeId)
        startActivity(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        cache?.flush()
    }
}