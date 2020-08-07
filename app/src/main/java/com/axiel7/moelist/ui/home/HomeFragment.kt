package com.axiel7.moelist.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AnimeRankingAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.ListAnimeAdapter
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
    private lateinit var recommendRecycler: RecyclerView
    private lateinit var animeRankingAdapter: AnimeRankingAdapter
    private lateinit var animeRecommendAdapter: ListAnimeAdapter
    private lateinit var animesRankingResponse: AnimeRankingResponse
    private lateinit var animesRecommendResponse: AnimeListResponse
    private var savedAnimesRankingResponse: AnimeRankingResponse? = null
    private var savedAnimesRecommendResponse: AnimeListResponse? = null
    private lateinit var animeListSeasonal: MutableList<AnimeRanking>
    private lateinit var animeListRecommend: MutableList<AnimeList>
    private lateinit var malApiService: MalApiService
    private lateinit var currentSeason: StartSeason
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var retrofit: Retrofit? = null
    private var cache: Cache? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        currentSeason = StartSeason(SeasonCalendar.getCurrentYear(), SeasonCalendar.getCurrentSeason())

        savedAnimesRankingResponse = sharedPref.getObject("animesRankingResponse", AnimeRankingResponse::class.java)
        savedAnimesRecommendResponse = sharedPref.getObject("animesRecommendResponse", AnimeListResponse::class.java)

        animeListSeasonal = mutableListOf(AnimeRanking(null, null), AnimeRanking(null, null),
            AnimeRanking(null, null), AnimeRanking(null, null))
        animeListRecommend = mutableListOf(AnimeList(null), AnimeList(null), AnimeList(null) ,AnimeList(null))

        if (savedAnimesRankingResponse!=null) {
            animesRankingResponse = savedAnimesRankingResponse as AnimeRankingResponse
            val animeListSeasonal2 = animesRankingResponse.data
            animeListSeasonal.clear()
            for (anime in animeListSeasonal2) {
                if (anime.node?.start_season==currentSeason) {
                    animeListSeasonal.add(anime)
                }
            }
        }
        if (savedAnimesRecommendResponse!=null) {
            animesRecommendResponse = savedAnimesRecommendResponse as AnimeListResponse
            animeListRecommend = animesRecommendResponse.data
        }


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
        animeRankingAdapter =
            context?.let {
                AnimeRankingAdapter(
                    animeListSeasonal,
                    R.layout.list_item_anime_seasonal,
                    it,
                    onClickListener = { _, animeRanking ->  openDetails(animeRanking.node?.id) }
                )
            }!!
        seasonRecycler.adapter = animeRankingAdapter

        recommendRecycler = view.findViewById(R.id.recommend_recycler)
        animeRecommendAdapter =
            context?.let {
                ListAnimeAdapter(
                    animeListRecommend,
                    R.layout.list_item_anime_seasonal,
                    it,
                    onClickListener = { _, animeList -> openDetails(animeList.node?.id) }
                )
            }!!
        animeRankingAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (animeListSeasonal.size <= 25) {
                    val nextPage: String = animesRankingResponse.paging.next
                    if (nextPage.isNotEmpty() || nextPage.isNotBlank()) {
                        val getMoreCall = malApiService.getNextRankingPage(nextPage)
                        initRankingCall(getMoreCall, false)
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
    private fun initRankingCall(call: Call<AnimeRankingResponse>?, shouldClear: Boolean) {
        call?.enqueue(object : Callback<AnimeRankingResponse> {
            override fun onResponse(call: Call<AnimeRankingResponse>, response: Response<AnimeRankingResponse>) {
                Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    animesRankingResponse = response.body()!!
                    val animeList2 = animesRankingResponse.data
                    if (!shouldClear || savedAnimesRankingResponse != animesRankingResponse) {
                        if (shouldClear) {
                            animeListSeasonal.clear()
                            sharedPref.saveObject("animesRankingResponse", animesRankingResponse)
                        }
                        for (anime in animeList2) {
                            if (anime.node?.start_season==currentSeason && !animeListSeasonal.contains(anime)) {
                                animeListSeasonal.add(anime)
                            }
                        }

                        animeRankingAdapter.notifyDataSetChanged()
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
            }

            override fun onFailure(call: Call<AnimeRankingResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
            }
        })
    }
    private fun initRecommendCall(call: Call<AnimeListResponse>?, shouldClear: Boolean) {
        call?.enqueue(object : Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    animesRecommendResponse = response.body()!!
                    val animeList2 = animesRecommendResponse.data
                    if (savedAnimesRecommendResponse != animesRecommendResponse) {
                        if (shouldClear) {
                            animeListRecommend.clear()
                            sharedPref.saveObject("animesRecommendResponse", animesRecommendResponse)
                        }
                        animeListRecommend.addAll(animeList2)
                        animeRecommendAdapter.setEndListReachedListener(object :EndListReachedListener {
                            override fun onBottomReached(position: Int) {
                                if (animeListRecommend.size <= 25) {
                                    val getMoreCall = malApiService.getNextRecommendPage(animesRecommendResponse.paging.next)
                                    initRecommendCall(getMoreCall, false)
                                }
                            }
                        })

                        animeRecommendAdapter.notifyDataSetChanged()
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
            }

            override fun onFailure(call: Call<AnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
            }
        })
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