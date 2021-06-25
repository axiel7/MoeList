package com.axiel7.moelist.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.UseCases
import com.axiel7.moelist.adapter.AiringAnimeAdapter
import com.axiel7.moelist.adapter.CurrentSeasonalAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.RecommendationsAdapter
import com.axiel7.moelist.databinding.FragmentHomeBinding
import com.axiel7.moelist.model.*
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.charts.RankingActivity
import com.axiel7.moelist.ui.charts.SeasonalActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate
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

    override fun setup() {
        binding.animeRank.setOnClickListener { openRanking("anime", it) }
        binding.mangaRank.setOnClickListener { openRanking("manga", it) }
        binding.seasonalChart.setOnClickListener { openSeasonChart(it) }
        binding.random.setOnClickListener { openDetails(-1, it) }

        if (isAdded) {
            if (animeListSeasonal.isEmpty()) {
                binding.loadingSeason.show()
            } else { binding.loadingSeason.hide() }
        }
        animeRankingAdapter = CurrentSeasonalAdapter(
            animeListSeasonal,
            onClickListener = { itemView, animeRanking ->  openDetails(animeRanking.node.id, itemView)})
        binding.seasonRecycler.adapter = animeRankingAdapter
        val seasonValue = "${StringFormat.formatSeason(currentSeason.season, requireContext())} ${currentSeason.year}"
        binding.seasonTitle.text = seasonValue
        binding.seasonTitle.setOnClickListener { openSeasonChart(it) }

        if (isAdded) {
            if (animeListRecommend.isEmpty()) {
                binding.loadingRecommend.show()
            } else { binding.loadingRecommend.hide() }
        }
        animeRecommendAdapter =
            RecommendationsAdapter(
                animeListRecommend,
                onClickListener = { itemView, animeList -> openDetails(animeList.node.id, itemView) }
            )
        animeRankingAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int, lastPosition: Int) {
                if (animesRankingResponse!=null && animeListSeasonal.size <= 25) {
                    val nextPage: String? = animesRankingResponse?.paging?.next
                    if (!nextPage.isNullOrEmpty()) {
                        val getMoreCall = malApiService.getNextAnimeRankingPage(nextPage)
                        initRankingCall(getMoreCall, false)
                    }
                }
            }
        })
        binding.recommendRecycler.adapter = animeRecommendAdapter

        if (isAdded) {
            if (todayList.isEmpty()) {
                binding.loadingToday.show()
            } else { binding.loadingToday.hide() }
        }
        todayAdapter =
            AiringAnimeAdapter(
                todayList,
                requireContext(),
                onClickListener = { itemView, animeList -> openDetails(animeList.node.id, itemView) }
            )
        todayAdapter.setEndListReachedListener(object: EndListReachedListener {
            override fun onBottomReached(position: Int, lastPosition: Int) {
                if (todayResponse!=null) {
                    val nextPage: String? = todayResponse?.paging?.next
                    if (!nextPage.isNullOrEmpty()) {
                        val getMoreCall = malApiService.getNextSeasonalPage(nextPage)
                        initTodayCall(getMoreCall, false)
                    }
                }
            }
        })
        binding.todayRecycler.adapter = todayAdapter
        binding.todayTitle.setOnClickListener { openToday() }

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
                        binding.loadingSeason.hide()
                        animeRankingAdapter.notifyDataSetChanged()
                    }
                    else {
                        binding.loadingSeason.hide()
                        animesRankingResponse = responseOld
                    }
                }
                else if (response.code()==401) {
                    if (isAdded) {
                        Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }

                val recommendCall = malApiService.getAnimeRecommend(30)
                initRecommendCall(recommendCall, true)
            }

            override fun onFailure(call: Call<AnimeRankingResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    if (animeListSeasonal.isNotEmpty()) { binding.loadingSeason.hide() }
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
                        binding.loadingRecommend.hide()
                        animeRecommendAdapter.notifyDataSetChanged()
                    }
                    else {
                        binding.loadingRecommend.hide()
                        animesRecommendResponse = responseOld
                    }

                    animeRecommendAdapter.setEndListReachedListener(object :EndListReachedListener {
                        override fun onBottomReached(position: Int, lastPosition: Int) {
                            if (animesRecommendResponse!=null && animeListRecommend.size <= 25) {
                                val getMoreCall = malApiService.getNextRecommendPage(animesRecommendResponse?.paging?.next!!)
                                initRecommendCall(getMoreCall, false)
                            }
                        }
                    })
                    binding.loadingRecommend.hide()
                }
                else if (response.code()==401) {
                    if (isAdded) {
                        Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<AnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    if (animeListRecommend.isNotEmpty()) { binding.loadingRecommend.hide() }
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
                            binding.todayRecycler.visibility = View.INVISIBLE
                            binding.loadingToday.hide()
                            binding.emptyToday.visibility = View.VISIBLE
                        }
                    }
                    else {
                        animeDb?.seasonalListDao()?.insertAllSeasonalAnimes(todayList)
                        todayList.sortByDescending { it.node.mean }
                        binding.loadingToday.hide()
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
                    binding.loadingToday.hide()
                    if (todayList.isEmpty()) { binding.emptyToday.visibility = View.VISIBLE }
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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