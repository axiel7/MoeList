package com.axiel7.moelist.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AiringAnimeAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.RecommendationsAdapter
import com.axiel7.moelist.adapter.SeasonalAnimeAdapter
import com.axiel7.moelist.model.*
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.MainActivity
import com.axiel7.moelist.ui.charts.RankingActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var seasonRecycler: RecyclerView
    private lateinit var seasonLoading: ContentLoadingProgressBar
    private lateinit var recommendRecycler: RecyclerView
    private lateinit var recommendLoading: ContentLoadingProgressBar
    private lateinit var todayRecycler: RecyclerView
    private lateinit var todayLoading: ContentLoadingProgressBar
    private lateinit var emptyToday: TextView
    private lateinit var animeRankingButton: MaterialCardView
    private lateinit var mangaRankingButton: MaterialCardView
    private lateinit var seasonalChartButton: MaterialCardView
    private lateinit var randomButton: MaterialCardView
    private lateinit var snackBarView: View
    private lateinit var animeRankingAdapter: SeasonalAnimeAdapter
    private lateinit var animeRecommendAdapter: RecommendationsAdapter
    private lateinit var todayAdapter: AiringAnimeAdapter
    private lateinit var animeListSeasonal: MutableList<AnimeRanking>
    private lateinit var animeListRecommend: MutableList<AnimeList>
    private lateinit var todayList: MutableList<SeasonalList>
    private lateinit var malApiService: MalApiService
    private lateinit var currentSeason: StartSeason
    private lateinit var jpDayWeek: String
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var animesRankingResponse: AnimeRankingResponse? = null
    private var animesRecommendResponse: AnimeListResponse? = null
    private var todayResponse: SeasonalAnimeResponse? = null
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        currentSeason = StartSeason(SeasonCalendar.getCurrentYear(), SeasonCalendar.getCurrentSeason())
        jpDayWeek = SeasonCalendar.getCurrentJapanWeekday()

        animeListSeasonal = animeDb?.rankingAnimeDao()?.getRankingAnimes()!!
        animeListRecommend = animeDb?.listAnimeDao()?.getListAnimes()!!

        todayList = mutableListOf()
        val todaySaved = animeDb?.seasonalListDao()?.getSeasonalAnimes()!!
        for (anime in todaySaved) {
            if (anime.node.broadcast!=null) {
                if (!todayList.contains(anime)
                    && anime.node.broadcast.day_of_the_week==jpDayWeek
                    && anime.node.start_season==currentSeason) {
                    todayList.add(anime)
                }
            }
        }
        todayList.sortByDescending { it.node.mean }

        createRetrofitAndApiService()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seasonRecycler = view.findViewById(R.id.season_recycler)
        seasonLoading = view.findViewById(R.id.loading_season)
        seasonLoading.show()
        animeRankingAdapter = AnimeRankingAdapter(
            animeListSeasonal,
            R.layout.list_item_anime_seasonal,
            onClickListener = { _, animeRanking ->  openDetails(animeRanking.node.id)})
        seasonRecycler.adapter = animeRankingAdapter

        recommendRecycler = view.findViewById(R.id.recommend_recycler)
        recommendLoading = view.findViewById(R.id.loading_recommend)
        emptyToday = view.findViewById(R.id.empty_today)
        if (animeListRecommend.isEmpty()) {
            recommendLoading.show()
        } else { seasonLoading.hide() }
        animeRecommendAdapter =
                RecommendationsAdapter(
                    animeListRecommend,
                    R.layout.list_item_anime_seasonal,
                    onClickListener = { _, animeList -> openDetails(animeList.node.id) }
                )
        animeRankingAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (animesRankingResponse!=null && animeListSeasonal.size <= 25) {
                    val nextPage: String? = animesRankingResponse!!.paging.next
                    if (nextPage?.isNotEmpty()!! || nextPage.isNotBlank()) {
                        val getMoreCall = malApiService.getNextRankingPage(nextPage)
                        initRankingCall(getMoreCall)
                    }
                }
            }
        })

        recommendRecycler.adapter = animeRecommendAdapter

        todayRecycler = view.findViewById(R.id.today_recycler)
        todayLoading = view.findViewById(R.id.loading_today)
        if (todayList.isEmpty()) {
            todayLoading.show()
        } else { seasonLoading.hide() }
        todayAdapter =
            AiringAnimeAdapter(
                todayList,
                R.layout.list_item_anime_today,
                onClickListener = { itemView, animeList -> openDetails(animeList.node.id, itemView) }
            )
        todayAdapter.setEndListReachedListener(object: EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (todayResponse!=null) {
                    val nextPage: String? = todayResponse!!.paging.next
                    if (!nextPage.isNullOrEmpty()) {
                        val getMoreCall = malApiService.getNextSeasonalPage(nextPage)
                        initTodayCall(getMoreCall, false)
                    }
                }
            }
        })
        todayRecycler.adapter = todayAdapter

        initCalls()
    }
    private fun initCalls() {
        val todayCall = malApiService.getSeasonalAnime(Urls.apiBaseUrl +
                "anime/season/${SeasonCalendar.getCurrentYear()}/${SeasonCalendar.getCurrentSeason()}",
            "anime_score", "broadcast,mean,start_season", 500)
        val rankingCall = malApiService.getAnimeRanking("airing","start_season", false)
        if (todayList.isNotEmpty()) {
            todayLoading.hide()
            initRankingCall(rankingCall, true)
        }
        else {
            initTodayCall(todayCall, true)
        }
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
    private fun initRankingCall(call: Call<AnimeRankingResponse>?) {
        call?.enqueue(object : Callback<AnimeRankingResponse> {
            override fun onResponse(call: Call<AnimeRankingResponse>, response: Response<AnimeRankingResponse>) {
                //Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    animesRankingResponse = response.body()!!
                    val animeList2 = animesRankingResponse!!.data
                    if (animeListSeasonal!=animeList2) {
                        for (anime in animeList2) {
                            if (!animeListSeasonal.contains(anime) &&
                                anime.node.start_season==currentSeason) {
                                animeListSeasonal.add(anime)
                            }
                        }
                        animeDb?.rankingAnimeDao()?.insertAllRankingAnimes(animeListSeasonal)
                    }
                    seasonLoading.hide()
                    animeRankingAdapter.notifyDataSetChanged()
                }
                //TODO (not tested)
                else if (response.code()==401) {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    sharedPref.saveString("accessToken", accessToken)
                    sharedPref.saveString("refreshToken", refreshToken)

                    call.clone()
                }
            }

            override fun onFailure(call: Call<AnimeRankingResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (animeListSeasonal.isNotEmpty()) { seasonLoading.hide() }
                if (isAdded) {
                    //Toast.makeText(context, "Error connecting to server", Toast.LENGTH_SHORT).show()
                    Snackbar.make(snackBarView, "Error connecting to server", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun initRecommendCall(call: Call<AnimeListResponse>?, shouldClear: Boolean) {
        call?.enqueue(object : Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                //Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    animesRecommendResponse = response.body()!!
                    val animeList2 = animesRecommendResponse!!.data
                    if (animeListRecommend!=animeList2) {
                        if (shouldClear) {
                            animeListRecommend.clear()
                        }
                        animeListRecommend.addAll(animeList2)
                        animeDb?.listAnimeDao()?.insertAllListAnimes(animeListRecommend)
                    }
                    animeRecommendAdapter.setEndListReachedListener(object :EndListReachedListener {
                        override fun onBottomReached(position: Int) {
                            if (animesRecommendResponse!=null && animeListRecommend.size <= 25) {
                                val getMoreCall = malApiService.getNextRecommendPage(animesRecommendResponse?.paging?.next!!)
                                initRecommendCall(getMoreCall, false)
                            }
                        }
                    })

                    recommendLoading.hide()
                    animeRecommendAdapter.notifyDataSetChanged()
                }
                //TODO (not tested)
                else if (response.code()==401) {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    sharedPref.saveString("accessToken", accessToken)
                    sharedPref.saveString("refreshToken", refreshToken)

                    call.clone()
                }
            }

            override fun onFailure(call: Call<AnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (animeListRecommend.isNotEmpty()) { recommendLoading.hide() }
                if (isAdded) {
                    //Toast.makeText(context, "Error connecting to server", Toast.LENGTH_SHORT).show()
                    Snackbar.make(snackBarView, "Error connecting to server", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun initTodayCall(call: Call<SeasonalAnimeResponse>?, shouldClear: Boolean) {
        call?.enqueue(object: Callback<SeasonalAnimeResponse> {
            override fun onResponse(
                call: Call<SeasonalAnimeResponse>,
                response: Response<SeasonalAnimeResponse>) {
                if (response.isSuccessful) {
                    todayResponse = response.body()
                    val animeList2 = todayResponse!!.data
                    if (shouldClear) {
                        animeDb?.seasonalListDao()?.deleteAllSeasonalAnimes(todayList)
                        todayList.clear()
                    }
                    for (anime in animeList2) {
                        if (anime.node.broadcast!=null) {
                            if (!todayList.contains(anime)
                                && anime.node.broadcast.day_of_the_week==jpDayWeek
                                && anime.node.start_season==currentSeason) {
                                todayList.add(anime)
                            }
                        }
                    }
                    if (todayList.isEmpty()) {
                        call.cancel()
                        val nextPage: String? = todayResponse!!.paging.next
                        if (!nextPage.isNullOrEmpty()) {
                            val getMoreCall = malApiService.getNextSeasonalPage(nextPage)
                            initTodayCall(getMoreCall, false)
                        }
                        else {
                            todayRecycler.visibility = View.INVISIBLE
                            todayLoading.visibility = View.INVISIBLE
                            emptyToday.visibility = View.VISIBLE
                        }
                    }
                    else {
                        animeDb?.seasonalListDao()?.insertAllSeasonalAnimes(todayList)
                        todayList.sortByDescending { it.node.mean }
                        todayLoading.hide()
                        todayAdapter.notifyDataSetChanged()
                    }
                }
                //TODO (not tested)
                else if (response.code()==401) {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    sharedPref.saveString("accessToken", accessToken)
                    sharedPref.saveString("refreshToken", refreshToken)

                    call.clone()
                }

                val rankingCall = malApiService.getAnimeRanking("airing","start_season", false)
                initRankingCall(rankingCall, true)
            }

            override fun onFailure(call: Call<SeasonalAnimeResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (todayList.isNotEmpty()) { todayLoading.hide() }
                if (isAdded) {
                    Snackbar.make(snackBarView, "Error connecting to server", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun openDetails(animeId: Int?) {
        val intent = Intent(context, AnimeDetailsActivity::class.java)
        intent.putExtra("animeId", animeId)
        startActivity(intent)
    }
}