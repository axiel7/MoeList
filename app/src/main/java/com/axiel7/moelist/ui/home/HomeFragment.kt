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
import com.axiel7.moelist.adapter.CurrentSeasonalAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.RecommendationsAdapter
import com.axiel7.moelist.model.*
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.MainActivity
import com.axiel7.moelist.ui.charts.RankingActivity
import com.axiel7.moelist.ui.charts.SeasonalActivity
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
    private lateinit var animeRankingAdapter: CurrentSeasonalAdapter
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

        animeListSeasonal = mutableListOf()
        val rankingAiring = animeDb?.rankingAnimeDao()?.getRankingAnimes("airing")!!
        for (anime in rankingAiring) {
            if (anime.node.start_season==currentSeason) {
                animeListSeasonal.add(anime)
            }
        }
        animeListRecommend = animeDb?.listAnimeDao()?.getListAnimes()!!

        todayList = mutableListOf()
        val todaySaved = animeDb?.seasonalListDao()?.getSeasonalAnimes()!!
        for (anime in todaySaved) {
            if (anime.node.broadcast!=null) {
                if (!todayList.contains(anime)
                    && anime.node.broadcast.day_of_the_week==jpDayWeek
                    && anime.node.start_season==currentSeason
                    && anime.node.status=="currently_airing") {
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
        animeRankingButton = view.findViewById(R.id.anime_rank)
        animeRankingButton.setOnClickListener { openRanking("anime", animeRankingButton) }
        mangaRankingButton = view.findViewById(R.id.manga_rank)
        mangaRankingButton.setOnClickListener { openRanking("manga", mangaRankingButton) }
        seasonalChartButton = view.findViewById(R.id.seasonal_chart)
        seasonalChartButton.setOnClickListener { openSeasonChart(seasonalChartButton) }

        randomButton = view.findViewById(R.id.random)
        randomButton.setOnClickListener { openDetails(-1, randomButton) }

        snackBarView = view
        seasonRecycler = view.findViewById(R.id.season_recycler)
        seasonLoading = view.findViewById(R.id.loading_season)
        if (animeListSeasonal.isEmpty()) {
            seasonLoading.show()
        } else { seasonLoading.hide() }
        animeRankingAdapter = CurrentSeasonalAdapter(
            animeListSeasonal,
            R.layout.list_item_anime,
            onClickListener = { itemView, animeRanking ->  openDetails(animeRanking.node.id, itemView)})
        seasonRecycler.adapter = animeRankingAdapter
        val seasonTitle = view.findViewById<TextView>(R.id.season_title)
        val seasonValue = "${StringFormat.formatSeason(currentSeason.season, requireContext())} ${currentSeason.year}"
        seasonTitle.text = seasonValue

        recommendRecycler = view.findViewById(R.id.recommend_recycler)
        recommendLoading = view.findViewById(R.id.loading_recommend)
        emptyToday = view.findViewById(R.id.empty_today)
        if (animeListRecommend.isEmpty()) {
            recommendLoading.show()
        } else { seasonLoading.hide() }
        animeRecommendAdapter =
                RecommendationsAdapter(
                    animeListRecommend,
                    R.layout.list_item_anime,
                    onClickListener = { itemView, animeList -> openDetails(animeList.node.id, itemView) }
                )
        animeRankingAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (animesRankingResponse!=null && animeListSeasonal.size <= 25) {
                    val nextPage: String? = animesRankingResponse?.paging?.next
                    if (!nextPage.isNullOrEmpty()) {
                        val getMoreCall = malApiService.getNextAnimeRankingPage(nextPage)
                        initRankingCall(getMoreCall, false)
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
                requireContext(),
                onClickListener = { itemView, animeList -> openDetails(animeList.node.id, itemView) }
            )
        todayAdapter.setEndListReachedListener(object: EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (todayResponse!=null) {
                    val nextPage: String? = todayResponse?.paging?.next
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
            "anime_score", "broadcast,mean,start_season,status", 500)
        //val rankingCall = malApiService.getAnimeRanking("airing","start_season", 200, false)
        initTodayCall(todayCall, true)
        /*if (todayList.isNotEmpty()) {
            todayLoading.hide()
            initRankingCall(rankingCall, true)
        }
        else {
            initTodayCall(todayCall, true)
        }*/
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
    private fun initRankingCall(call: Call<AnimeRankingResponse>?, shouldClear: Boolean) {
        call?.enqueue(object : Callback<AnimeRankingResponse> {
            override fun onResponse(
                call: Call<AnimeRankingResponse>,
                response: Response<AnimeRankingResponse>,
            ) {
                //Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    val responseOld = ResponseConverter
                        .stringToAnimeRankResponse(sharedPref.getString("animeRankingResponse", ""))

                    if (responseOld!=response.body() || animeListSeasonal.isEmpty()) {
                        animesRankingResponse = response.body()

                        val animeList2 = animesRankingResponse!!.data
                        if (shouldClear) {
                            sharedPref.saveString("animeRankingResponse",
                                ResponseConverter.animeRankResponseToString(animesRankingResponse))
                            animeDb?.rankingAnimeDao()?.deleteAllRankingAnimes(animeListSeasonal)
                            animeListSeasonal.clear()
                        }
                        for (anime in animeList2) {
                            anime.ranking_type = "airing"
                            if (anime.node.start_season==currentSeason) {
                                animeListSeasonal.add(anime)
                            }
                        }
                        //animeListSeasonal.addAll(animeList2)
                        animeDb?.rankingAnimeDao()?.insertAllRankingAnimes(animeList2)
                        seasonLoading.hide()
                        animeRankingAdapter.notifyDataSetChanged()
                    }
                    else {
                        seasonLoading.hide()
                        animesRankingResponse = responseOld
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

                val recommendCall = malApiService.getAnimeRecommend(30)
                initRecommendCall(recommendCall, true)
            }

            override fun onFailure(call: Call<AnimeRankingResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (animeListSeasonal.isNotEmpty()) { seasonLoading.hide() }
                if (isAdded) {
                    Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
                val recommendCall = malApiService.getAnimeRecommend(30)
                initRecommendCall(recommendCall, true)
            }
        })
    }
    private fun initRecommendCall(call: Call<AnimeListResponse>?, shouldClear: Boolean) {
        call?.enqueue(object : Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                //Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {

                    val responseOld = ResponseConverter
                        .stringToAnimeListResponse(sharedPref.getString("animeRecommendResponse", ""))

                    if (responseOld!=response.body() || animeListRecommend.isEmpty()) {
                        animesRecommendResponse = response.body()

                        val animeList2 = animesRecommendResponse!!.data
                        if (shouldClear) {
                            sharedPref.saveString("animeRecommendResponse",
                                ResponseConverter.animeListResponseToString(animesRecommendResponse))
                            animeDb?.listAnimeDao()?.deleteAllListAnimes(animeListRecommend)
                            animeListRecommend.clear()
                        }
                        animeListRecommend.addAll(animeList2)
                        animeDb?.listAnimeDao()?.insertAllListAnimes(animeList2)
                        recommendLoading.hide()
                        animeRecommendAdapter.notifyDataSetChanged()
                    }
                    else {
                        recommendLoading.hide()
                        animesRecommendResponse = responseOld
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
                    Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
                                && anime.node.start_season==currentSeason
                                && anime.node.status == "currently_airing") {
                                todayList.add(anime)
                            }
                        }
                    }
                    if (todayList.isEmpty()) {
                        call.cancel()
                        val nextPage: String? = todayResponse!!.paging?.next
                        if (!nextPage.isNullOrEmpty()) {
                            val getMoreCall = malApiService.getNextSeasonalPage(nextPage)
                            //initTodayCall(getMoreCall, false)
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

                val rankingCall = malApiService.getAnimeRanking("airing","start_season", 200,false)
                initRankingCall(rankingCall, true)
            }

            override fun onFailure(call: Call<SeasonalAnimeResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                todayLoading.hide()
                if (todayList.isEmpty()) { emptyToday.visibility = View.VISIBLE }
                if (isAdded) {
                    Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
                val rankingCall = malApiService.getAnimeRanking("airing","start_season", 200,false)
                initRankingCall(rankingCall, true)
            }
        })
    }

    private fun openDetails(animeId: Int?, view: View?) {
        if (view!=null) {
            val poster = view.findViewById<ShapeableImageView>(R.id.anime_poster)
            val intent = Intent(context, AnimeDetailsActivity::class.java)
            val bundle = if (poster!=null) {
                ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.transitionName)
            } else {
                ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, view.transitionName)
            }
            intent.putExtra("animeId", animeId)
            startActivity(intent, bundle.toBundle())
        }
        else {
            val intent = Intent(context, AnimeDetailsActivity::class.java)
            intent.putExtra("animeId", animeId)
            startActivity(intent)
        }
    }
    private fun openRanking(type: String, view: View) {
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, view.transitionName)
        val intent = Intent(context, RankingActivity::class.java)
        intent.putExtra("mediaType", type)
        startActivity(intent, bundle.toBundle())
    }
    private fun openSeasonChart(view: View) {
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, view.transitionName)
        val intent = Intent(context, SeasonalActivity::class.java)
        startActivity(intent, bundle.toBundle())
    }
}