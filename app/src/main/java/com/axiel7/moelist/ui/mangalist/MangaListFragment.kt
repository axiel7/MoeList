package com.axiel7.moelist.ui.mangalist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.MyMangaListAdapter
import com.axiel7.moelist.adapter.PlusButtonTouchedListener
import com.axiel7.moelist.model.MyMangaListStatus
import com.axiel7.moelist.model.UserMangaList
import com.axiel7.moelist.model.UserMangaListResponse
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.MainActivity
import com.axiel7.moelist.ui.details.MangaDetailsActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MangaListFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var mangaListRecycler: RecyclerView
    private lateinit var filtersFab: FloatingActionButton
    private lateinit var loadingBar: ContentLoadingProgressBar
    private lateinit var snackBarView: View
    private lateinit var mangaListAdapter: MyMangaListAdapter
    private var mangaListResponse: UserMangaListResponse? = null
    private lateinit var mangaList: MutableList<UserMangaList>
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

        defaultStatus = sharedPref.getInt("checkedStatusButtonManga", R.id.reading_button)
        if (defaultStatus==null || defaultStatus==0) {
            defaultStatus = R.id.reading_button
        }
        changeStatusFilter(defaultStatus!!)

        if (MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!=null) {
            mangaList = MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!!
        }

        createRetrofitAndApiService()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_mangalist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackBarView = view
        loadingBar = view.findViewById(R.id.loading_mangalist)
        loadingBar.hide()
        if (mangaList.isEmpty()) {
            loadingBar.show()
        }

        mangaListRecycler = view.findViewById(R.id.mangalist_recycler)
        mangaListAdapter =
            MyMangaListAdapter(
                mangaList,
                R.layout.list_item_mangalist,
                onClickListener = { itemView, userMangaList -> openDetails(userMangaList.node.id, itemView) }
            )
        mangaListAdapter.setEndListReachedListener(object : EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (mangaListResponse!=null) {
                    val nextPage: String? = mangaListResponse!!.paging.next
                    if (nextPage!=null) {
                        val getMoreCall = malApiService.getNextMangaListPage(nextPage)
                        initMangaListCall(getMoreCall, false)
                    }
                }
            }
        })
        mangaListAdapter.setPlusButtonTouchedListener(object : PlusButtonTouchedListener {
            override fun onButtonTouched(view: View, position: Int) {
                val animeId = mangaListAdapter.getAnimeId(position)
                val watchedEpisodes = mangaListAdapter.getWatchedEpisodes(position)
                val totalEpisodes = mangaListAdapter.getTotalEpisodes(position)
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

        mangaListRecycler.adapter = mangaListAdapter


        filtersFab = view.findViewById(R.id.filters_fab)
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filters_manga, null)
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
            if (MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!=null) {
                mangaList.clear()
                val mangaList2 = MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!!
                mangaList.addAll(mangaList2)
                mangaListAdapter.notifyDataSetChanged()
            }
            initCalls()
        }

        mangaListRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        val mangaListCall = malApiService.getUserMangaList(listStatus, "list_status,num_chapters,media_type,status", "manga_title")
        initMangaListCall(mangaListCall, true)
    }
    private fun initMangaListCall(call: Call<UserMangaListResponse>, shouldClear: Boolean) {
        call.enqueue(object: Callback<UserMangaListResponse> {
            override fun onResponse(call: Call<UserMangaListResponse>, response: Response<UserMangaListResponse>) {
                //Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    val responseOld = ResponseConverter
                        .stringToUserMangaListResponse(sharedPref.getString("mangaListResponse$listStatus", ""))
                    if (responseOld!=response.body() || mangaList.isEmpty()) {
                        mangaListResponse = response.body()

                        val mangaList2 = mangaListResponse!!.data
                        for (manga in mangaList2) {
                            manga.status = manga.list_status?.status
                        }
                        if (shouldClear) {
                            sharedPref.saveString("mangaListResponse$listStatus",
                                ResponseConverter.userMangaListResponseToString(mangaListResponse))
                            mangaList.clear()
                        }
                        mangaList.addAll(mangaList2)
                        loadingBar.hide()
                        MyApplication.animeDb?.userMangaListDao()?.insertUserMangaList(mangaList)
                        mangaListAdapter.notifyDataSetChanged()
                    }
                    else {
                        mangaListResponse = responseOld
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

            override fun onFailure(call: Call<UserMangaListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    Snackbar.make(snackBarView, "Error connecting to server", Snackbar.LENGTH_SHORT).show()
                }
            }

        })
    }
    private fun addOneEpisode(mangaId: Int, chaptersRead: Int?, status: String?) {
        loadingBar.show()
        val shouldNotUpdate = chaptersRead==null
        if (!shouldNotUpdate) {
            val updateListCall = malApiService
                .updateMangaList(Urls.apiBaseUrl + "manga/$mangaId/my_list_status", status, null, chaptersRead, null)
            updateListCall.enqueue(object : Callback<MyMangaListStatus> {
                override fun onResponse(call: Call<MyMangaListStatus>, response: Response<MyMangaListStatus>) {
                    if (response.isSuccessful) {
                        //val myListStatus = response.body()!!
                        initCalls()
                        if (isAdded) {
                            Snackbar.make(snackBarView, "Updated", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    else if (isAdded) {
                        Snackbar.make(snackBarView, "Error updating list", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyMangaListStatus>, t: Throwable) {
                    Log.d("MoeLog", t.toString())
                    if (isAdded) {
                        Snackbar.make(snackBarView, "Error updating list", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            loadingBar.hide()
            if (isAdded) {
                Snackbar.make(snackBarView, "No changes", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun changeStatusFilter(radioButton: Int) {
        listStatus = when(radioButton) {
            R.id.reading_button -> "reading"
            R.id.completed_button -> "completed"
            R.id.onhold_button -> "on_hold"
            R.id.dropped_button -> "dropped"
            R.id.ptr_button -> "plan_to_read"
            else -> "reading"
        }
    }
    private fun openDetails(mangaId: Int?, view: View?) {
        if (view!=null) {
            val poster = view.findViewById<FrameLayout>(R.id.poster_container)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.transitionName)
            val intent = Intent(context, MangaDetailsActivity::class.java)
            intent.putExtra("mangaId", mangaId)
            startActivityForResult(intent, 17, bundle.toBundle())
        }
        else {
            val intent = Intent(context, MangaDetailsActivity::class.java)
            intent.putExtra("mangaId", mangaId)
            startActivityForResult(intent, 17)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==17 && resultCode== Activity.RESULT_OK) {
            val shouldUpdate :Boolean = data?.extras?.get("entryUpdated") as Boolean
            if (shouldUpdate) {
                initCalls()
            }
        }
    }
}