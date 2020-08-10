package com.axiel7.moelist.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AnimeRankingAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.RecommendationsAdapter
import com.axiel7.moelist.model.*
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.AnimeDetailsActivity
import com.axiel7.moelist.utils.*
import com.axiel7.moelist.utils.CreateOkHttpClient.createOkHttpClient
import okhttp3.Cache
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
    private lateinit var animeRankingAdapter: AnimeRankingAdapter
    private lateinit var animeRecommendAdapter: RecommendationsAdapter
    private lateinit var animeListSeasonal: MutableList<AnimeRanking>
    private lateinit var animeListRecommend: MutableList<AnimeList>
    private lateinit var malApiService: MalApiService
    private lateinit var currentSeason: StartSeason
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var animesRankingResponse: AnimeRankingResponse? = null
    private var animesRecommendResponse: AnimeListResponse? = null
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        currentSeason = StartSeason(SeasonCalendar.getCurrentYear(), SeasonCalendar.getCurrentSeason())

        animeListSeasonal = animeDb?.rankingAnimeDao()?.getRankingAnimes()!!
        animeListRecommend = animeDb?.listAnimeDao()?.getListAnimes()!!

        cache = context?.let { GetCacheFile.getCacheFile(it, 20) }

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
        recommendLoading.show()
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
        val rankingCall = malApiService.getAnimeRanking("airing","start_season")
        val recommendCall = malApiService.getAnimeRecommend(30)
        initRankingCall(rankingCall, true)
        initRecommendCall(recommendCall, true)
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
            }
        })
    }

    private fun openDetails(animeId: Int?) {
        val intent = Intent(context, AnimeDetailsActivity::class.java)
        intent.putExtra("animeId", animeId)
        startActivity(intent)
    }
}