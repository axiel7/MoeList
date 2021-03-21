package com.axiel7.moelist.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.UseCases
import com.axiel7.moelist.adapter.AiringAnimeAdapter
import com.axiel7.moelist.adapter.CurrentSeasonalAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.RecommendationsAdapter
import com.axiel7.moelist.model.*
import com.axiel7.moelist.ui.charts.RankingActivity
import com.axiel7.moelist.ui.charts.SeasonalActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var animeRankingAdapter: CurrentSeasonalAdapter
    private lateinit var animeRecommendAdapter: RecommendationsAdapter
    private lateinit var todayAdapter: AiringAnimeAdapter
    private lateinit var animeListSeasonal: MutableList<AnimeRanking>
    private lateinit var animeListRecommend: MutableList<AnimeList>
    private lateinit var todayList: MutableList<SeasonalList>
    private lateinit var currentSeason: StartSeason
    private lateinit var jpDayWeek: String
    private var animesRankingResponse: AnimeRankingResponse? = null
    private var animesRecommendResponse: AnimeListResponse? = null
    private var todayResponse: SeasonalAnimeResponse? = null
    private var showNsfw = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = SharedPrefsHelpers.instance!!
        showNsfw = if (sharedPref.getBoolean("nsfw", false)) { 1 } else { 0 }

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
        anime_rank.setOnClickListener { openRanking("anime", anime_rank) }
        manga_rank.setOnClickListener { openRanking("manga", manga_rank) }
        seasonal_chart.setOnClickListener { openSeasonChart(seasonal_chart) }
        random.setOnClickListener { openDetails(-1, random) }

        if (animeListSeasonal.isEmpty()) {
            loading_season.show()
        } else { loading_season.hide() }
        animeRankingAdapter = CurrentSeasonalAdapter(
            animeListSeasonal,
            R.layout.list_item_anime,
            onClickListener = { itemView, animeRanking ->  openDetails(animeRanking.node.id, itemView)})
        season_recycler.adapter = animeRankingAdapter
        val seasonTitle = view.findViewById<TextView>(R.id.season_title)
        val seasonValue = "${StringFormat.formatSeason(currentSeason.season, requireContext())} ${currentSeason.year}"
        seasonTitle.text = seasonValue
        seasonTitle.setOnClickListener { openSeasonChart(seasonTitle) }

        if (animeListRecommend.isEmpty()) {
            loading_recommend.show()
        } else { loading_season.hide() }
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
        recommend_recycler.adapter = animeRecommendAdapter

        if (todayList.isEmpty()) {
            loading_today.show()
        } else { loading_season.hide() }
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
        today_recycler.adapter = todayAdapter
        val todayTitle = view.findViewById<TextView>(R.id.today_title)
        todayTitle.setOnClickListener { openToday() }

        initCalls()
    }
    private fun initCalls() {
        val todayCall = malApiService.getSeasonalAnime(Urls.apiBaseUrl +
                "anime/season/${SeasonCalendar.getCurrentYear()}/${SeasonCalendar.getCurrentSeason()}",
            "anime_score", "broadcast,mean,start_season,status", 500, showNsfw)
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
    private fun initRankingCall(call: Call<AnimeRankingResponse>?, shouldClear: Boolean) {
        call?.enqueue(object : Callback<AnimeRankingResponse> {
            override fun onResponse(
                call: Call<AnimeRankingResponse>,
                response: Response<AnimeRankingResponse>, ) {
                if (response.isSuccessful && isAdded) {
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
                        animeDb?.rankingAnimeDao()?.insertAllRankingAnimes(animeList2)
                        loading_season.hide()
                        animeRankingAdapter.notifyDataSetChanged()
                    }
                    else {
                        loading_season.hide()
                        animesRankingResponse = responseOld
                    }
                }
                else if (response.code()==401) {
                    if (isAdded) {
                        Snackbar.make(home_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }

                val recommendCall = malApiService.getAnimeRecommend(30)
                initRecommendCall(recommendCall, true)
            }

            override fun onFailure(call: Call<AnimeRankingResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (animeListSeasonal.isNotEmpty()) { loading_season.hide() }
                if (isAdded) {
                    Snackbar.make(home_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
                val recommendCall = malApiService.getAnimeRecommend(30)
                initRecommendCall(recommendCall, true)
            }
        })
    }
    private fun initRecommendCall(call: Call<AnimeListResponse>?, shouldClear: Boolean) {
        call?.enqueue(object : Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                if (response.isSuccessful && isAdded) {
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
                        loading_recommend.hide()
                        animeRecommendAdapter.notifyDataSetChanged()
                    }
                    else {
                        loading_recommend.hide()
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
                    loading_recommend.hide()
                }
                else if (response.code()==401) {
                    if (isAdded) {
                        Snackbar.make(home_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<AnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (animeListRecommend.isNotEmpty()) { loading_recommend.hide() }
                if (isAdded) {
                    Snackbar.make(home_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun initTodayCall(call: Call<SeasonalAnimeResponse>?, shouldClear: Boolean) {
        call?.enqueue(object: Callback<SeasonalAnimeResponse> {
            override fun onResponse(
                call: Call<SeasonalAnimeResponse>,
                response: Response<SeasonalAnimeResponse>) {
                if (response.isSuccessful && isAdded) {
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
                        if (nextPage.isNullOrEmpty()) {
                            today_recycler.visibility = View.INVISIBLE
                            loading_today.visibility = View.INVISIBLE
                            empty_today.visibility = View.VISIBLE
                        }
                    }
                    else {
                        animeDb?.seasonalListDao()?.insertAllSeasonalAnimes(todayList)
                        todayList.sortByDescending { it.node.mean }
                        loading_today.hide()
                        todayAdapter.notifyDataSetChanged()
                    }
                }
                else if (response.code()==401) {
                    if (isAdded) {
                        UseCases.logOut(requireContext())
                    }
                }

                val rankingCall = malApiService.getAnimeRanking("airing","start_season", 200, showNsfw)
                initRankingCall(rankingCall, true)
            }

            override fun onFailure(call: Call<SeasonalAnimeResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    loading_today.hide()
                    if (todayList.isEmpty()) { empty_today.visibility = View.VISIBLE }
                    Snackbar.make(home_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    val rankingCall = malApiService.getAnimeRanking("airing","start_season", 200, showNsfw)
                    initRankingCall(rankingCall, true)
                }
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
                ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
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
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
        val intent = Intent(context, RankingActivity::class.java)
        intent.putExtra("mediaType", type)
        startActivity(intent, bundle.toBundle())
    }
    private fun openSeasonChart(view: View) {
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
        val intent = Intent(context, SeasonalActivity::class.java)
        startActivity(intent, bundle.toBundle())
    }
    private fun openToday() {
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
        val intent = Intent(context, TodayActivity::class.java)
        startActivity(intent, bundle.toBundle())
    }
}