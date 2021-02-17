package com.axiel7.moelist.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AiringAnimeAdapter
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.model.SeasonalAnimeResponse
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.model.StartSeason
import com.axiel7.moelist.ui.BaseActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.SeasonCalendar
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.android.synthetic.main.activity_seasonal.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TodayActivity : BaseActivity() {

    private lateinit var todayAdapter: AiringAnimeAdapter
    private lateinit var todayList: MutableList<SeasonalList>
    private lateinit var jpDayWeek: String
    private lateinit var currentSeason: StartSeason
    private var todayResponse: SeasonalAnimeResponse? = null
    private var showNsfw = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seasonal)

        window.statusBarColor = getColorFromAttr(R.attr.colorToolbar)

        seasonal_toolbar.title = getString(R.string.today)
        setSupportActionBar(seasonal_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        seasonal_toolbar.setNavigationOnClickListener { onBackPressed() }

        showNsfw = if (SharedPrefsHelpers.instance!!.getBoolean("nsfw", false)) { 1 } else { 0 }

        currentSeason = StartSeason(SeasonCalendar.getCurrentYear(), SeasonCalendar.getCurrentSeason())
        jpDayWeek = SeasonCalendar.getCurrentJapanWeekday()

        todayList = mutableListOf()
        val todaySaved = MyApplication.animeDb?.seasonalListDao()?.getSeasonalAnimes()!!
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

        todayAdapter = AiringAnimeAdapter(
            todayList,
            R.layout.list_item_anime_today_extended,
            this,
            onClickListener = {itemView, animeList -> openDetails(animeList.node.id, itemView)})
        todayAdapter.setEndListReachedListener(object : EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (todayResponse!=null) {
                    val nextPage = todayResponse?.paging?.next
                    if (nextPage!=null && nextPage.isNotEmpty()) {
                        val getMoreCall = MyApplication.malApiService.getNextSeasonalPage(nextPage)
                        initCall(getMoreCall, false)
                    }
                }
            }
        })
        seasonal_recycler.adapter = todayAdapter

        seasonal_loading.hide()
        val filterFab = findViewById<FloatingActionButton>(R.id.filter_fab)
        filterFab.visibility = View.GONE

        val todayCall = MyApplication.malApiService.getSeasonalAnime(Urls.apiBaseUrl +
                "anime/season/${SeasonCalendar.getCurrentYear()}/${SeasonCalendar.getCurrentSeason()}",
            "anime_score", "broadcast,mean,start_season,status", 500, showNsfw)
        if (todayList.isEmpty()) { initCall(todayCall, true) }
    }

    private fun initCall(call: Call<SeasonalAnimeResponse>?, shouldClear: Boolean) {
        seasonal_loading.show()
        call?.enqueue(object: Callback<SeasonalAnimeResponse> {
            override fun onResponse(
                call: Call<SeasonalAnimeResponse>,
                response: Response<SeasonalAnimeResponse>
            ) {
                if (response.isSuccessful) {
                    todayResponse = response.body()
                    val animeList2 = todayResponse!!.data
                    if (shouldClear) {
                        MyApplication.animeDb?.seasonalListDao()?.deleteAllSeasonalAnimes(todayList)
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
                            seasonal_recycler.visibility = View.INVISIBLE
                            seasonal_loading.visibility = View.INVISIBLE
                        }
                    }
                    else {
                        MyApplication.animeDb?.seasonalListDao()?.insertAllSeasonalAnimes(todayList)
                        todayList.sortByDescending { it.node.mean }
                        seasonal_loading.hide()
                        todayAdapter.notifyDataSetChanged()
                    }
                }
                else if (response.code()==401) {
                    Snackbar.make(seasonal_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SeasonalAnimeResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                seasonal_loading.hide()
                Snackbar.make(seasonal_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun openDetails(animeId: Int?, view: View) {
        val poster = view.findViewById<ImageView>(R.id.anime_poster)
        val intent = Intent(this, AnimeDetailsActivity::class.java)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, poster, poster.transitionName)
        intent.putExtra("animeId", animeId)
        startActivity(intent, bundle.toBundle())
    }
}