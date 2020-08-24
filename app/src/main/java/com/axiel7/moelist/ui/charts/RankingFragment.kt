package com.axiel7.moelist.ui.charts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AnimeRankingAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.MangaRankingAdapter
import com.axiel7.moelist.model.AnimeRanking
import com.axiel7.moelist.model.AnimeRankingResponse
import com.axiel7.moelist.model.MangaRanking
import com.axiel7.moelist.model.MangaRankingResponse
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.MainActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.ui.details.MangaDetailsActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RankingFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var malApiService: MalApiService
    private lateinit var recyclerRank: RecyclerView
    private lateinit var rankingAnime: MutableList<AnimeRanking>
    private lateinit var rankingManga: MutableList<MangaRanking>
    private lateinit var animeRankingAdapter: AnimeRankingAdapter
    private lateinit var mangaRankingAdapter: MangaRankingAdapter
    private lateinit var loadingBar: ContentLoadingProgressBar
    private lateinit var snackBarView: View
    private lateinit var mediaType: String
    private var rankType: String = "all"
    private var showNsfw = false
    private var animeResponse: AnimeRankingResponse? = null
    private var mangaResponse: MangaRankingResponse? = null
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        showNsfw = sharedPreferences.getBoolean("nsfw", false)
        mediaType = arguments?.getString("mediaType", "anime")!!
        rankType = arguments?.getString("rankType", "all")!!

        when(mediaType) {
            "anime" -> rankingAnime = animeDb?.rankingAnimeDao()?.getRankingAnimes(rankType)!!
            "manga" -> rankingManga = animeDb?.rankingMangaDao()?.getRankingMangas(rankType)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        snackBarView = view.findViewById(R.id.ranking_layout)
        //(snackBarView as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        loadingBar = view.findViewById(R.id.ranking_loading)

        recyclerRank = view.findViewById(R.id.recycler_ranking)
        when(mediaType) {
            "anime" -> {
                if (rankingAnime.isEmpty()) { loadingBar.show() }
                animeRankingAdapter = AnimeRankingAdapter(
                    rankingAnime,
                    R.layout.list_item_ranking,
                    requireContext(),
                    onClickListener = {itemView, animeRanking -> openDetails(animeRanking.node.id, itemView)})
                animeRankingAdapter.setEndListReachedListener(object :EndListReachedListener {
                    override fun onBottomReached(position: Int) {
                        if (animeResponse!=null) {
                            val nextPage = animeResponse?.paging?.next
                            if (nextPage!=null && nextPage.isNotEmpty()) {
                                val getMoreCall = malApiService.getNextAnimeRankingPage(nextPage)
                                enqueueAnimeCall(getMoreCall, false)
                            }
                        }
                    }
                })
                recyclerRank.adapter = animeRankingAdapter
            }
            "manga" -> {
                if (rankingManga.isEmpty()) { loadingBar.show() }
                mangaRankingAdapter = MangaRankingAdapter(
                    rankingManga,
                    R.layout.list_item_ranking,
                    requireContext(),
                    onClickListener = {itemView, mangaRanking -> openDetails(mangaRanking.node.id, itemView)})
                mangaRankingAdapter.setEndListReachedListener(object :EndListReachedListener {
                    override fun onBottomReached(position: Int) {
                        if (mangaResponse!=null) {
                            val nextPage = mangaResponse?.paging?.next
                            if (nextPage!=null && nextPage.isNotEmpty()) {
                                val getMoreCall = malApiService.getNextMangaRankingPage(nextPage)
                                enqueueMangaCall(getMoreCall, false)
                            }
                        }
                    }
                })
                recyclerRank.adapter = mangaRankingAdapter
            }
        }

        createRetrofitAndApiService()
        initCall()
    }
    private fun createRetrofitAndApiService() {
        if (retrofit==null) {
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
        }
        malApiService = retrofit?.create(MalApiService::class.java)!!
    }
    private fun initCall() {
        when(mediaType) {
            "anime" -> {
                val animeCall = malApiService.getAnimeRanking(rankType, "mean,media_type,num_episodes,num_list_users", showNsfw)
                enqueueAnimeCall(animeCall, true)
            }
            "manga" -> {
                val mangaCall = malApiService.getMangaRanking(rankType, "mean,media_type,num_chapters,num_list_users", showNsfw)
                enqueueMangaCall(mangaCall, true)
            }
        }
    }
    private fun enqueueAnimeCall(call: Call<AnimeRankingResponse>, shouldClear: Boolean) {
        call.enqueue(object :Callback<AnimeRankingResponse> {
            override fun onResponse(
                call: Call<AnimeRankingResponse>,
                response: Response<AnimeRankingResponse>) {
                if (response.isSuccessful) {
                    val responseOld = ResponseConverter
                        .stringToAnimeRankResponse(sharedPref.getString("animeRankingResponse$rankType", ""))

                    if (responseOld!=response.body() || rankingAnime.isEmpty()) {
                        Log.d("MoeLog", "new response")
                        animeResponse = response.body()!!
                        val animeList = animeResponse!!.data
                        for (anime in animeList) {
                            anime.ranking_type = rankType
                        }
                        if (shouldClear) {
                            sharedPref.saveString("animeRankingResponse$rankType",
                                ResponseConverter.animeRankResponseToString(animeResponse))
                            animeDb?.rankingAnimeDao()?.deleteAllRankingAnimes(rankingAnime)
                            if (rankingAnime.isEmpty()) {
                                animeDb?.rankingAnimeDao()?.insertAllRankingAnimes(animeList)
                            }
                            rankingAnime.clear()
                        }
                        rankingAnime.addAll(animeList)
                        animeRankingAdapter.notifyDataSetChanged()
                    } else {
                        animeResponse = responseOld
                    }
                }
                else if (response.code()==401) {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    sharedPref.saveString("accessToken", accessToken)
                    sharedPref.saveString("refreshToken", refreshToken)

                    call.clone()
                }
                loadingBar.hide()
            }

            override fun onFailure(call: Call<AnimeRankingResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                loadingBar.hide()
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }
    private fun enqueueMangaCall(call: Call<MangaRankingResponse>, shouldClear: Boolean) {
        call.enqueue(object :Callback<MangaRankingResponse> {
            override fun onResponse(
                call: Call<MangaRankingResponse>,
                response: Response<MangaRankingResponse>) {
                if (response.isSuccessful) {
                    val responseOld = ResponseConverter
                        .stringToMangaRankResponse(sharedPref.getString("mangaRankingResponse$rankType", ""))

                    if (responseOld!=response.body() || rankingManga.isEmpty()) {
                        mangaResponse = response.body()!!
                        val mangaList = mangaResponse!!.data
                        for (manga in mangaList) {
                            manga.ranking_type = rankType
                        }
                        if (shouldClear) {
                            sharedPref.saveString("mangaRankingResponse$rankType",
                                ResponseConverter.mangaRankResponseToString(mangaResponse))
                            animeDb?.rankingMangaDao()?.deleteAllRankingMangas(rankingManga)
                            if (rankingManga.isEmpty()) {
                                animeDb?.rankingMangaDao()?.insertAllRankingMangas(mangaList)
                            }
                            rankingManga.clear()
                        }
                        rankingManga.addAll(mangaList)
                        mangaRankingAdapter.notifyDataSetChanged()
                    } else {
                        mangaResponse = responseOld
                    }
                }
                else if (response.code()==401) {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    sharedPref.saveString("accessToken", accessToken)
                    sharedPref.saveString("refreshToken", refreshToken)

                    call.clone()
                }
                loadingBar.hide()
            }
            override fun onFailure(call: Call<MangaRankingResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                loadingBar.hide()
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }
    private fun openDetails(id: Int, view: View) {
        val poster = view.findViewById<FrameLayout>(R.id.poster_container)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.transitionName)
        when(mediaType) {
            "anime" -> {
                val intent = Intent(context, AnimeDetailsActivity::class.java)
                intent.putExtra("animeId", id)
                startActivity(intent, bundle.toBundle())
            }
            "manga" -> {
                val intent = Intent(context, MangaDetailsActivity::class.java)
                intent.putExtra("mangaId", id)
                startActivity(intent, bundle.toBundle())
            }
        }
    }
}