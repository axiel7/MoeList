package com.axiel7.moelist.ui.charts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.SeasonalAnimeAdapter
import com.axiel7.moelist.model.SeasonalAnimeResponse
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.model.StartSeason
import com.axiel7.moelist.ui.BaseActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.InsetsHelper.addSystemWindowInsetToMargin
import com.axiel7.moelist.utils.SeasonCalendar
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class SeasonalActivity : BaseActivity() {

    //private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var toolbar: Toolbar
    private lateinit var seasonalRecycler: RecyclerView
    private lateinit var seasonalAdapter: SeasonalAnimeAdapter
    private lateinit var seasonalList: MutableList<SeasonalList>
    private lateinit var filterFab: FloatingActionButton
    private lateinit var snackBarView: View
    private lateinit var loadingBar: ContentLoadingProgressBar
    private lateinit var seasonLayout: TextInputLayout
    private lateinit var yearLayout: TextInputLayout
    private lateinit var season: String
    private lateinit var currentSeason: StartSeason
    private var year by Delegates.notNull<Int>()
    private var animeResponse: SeasonalAnimeResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seasonal)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorCard)

        toolbar = findViewById(R.id.seasonal_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        //SharedPrefsHelpers.init(this)
        //sharedPref = SharedPrefsHelpers.instance!!

        season = SeasonCalendar.getCurrentSeason()
        year = SeasonCalendar.getCurrentYear()
        currentSeason = StartSeason(year, season)
        toolbar.title = "${StringFormat.formatSeason(season, this)} $year"

        seasonalList = mutableListOf()
        val savedResponse = animeDb?.seasonalResponseDao()?.getSeasonalResponse(season, year)
        if (savedResponse!=null) {
            animeResponse = savedResponse
            val savedList = animeResponse!!.data
            for (anime in savedList) {
                if (anime.node.start_season==currentSeason) {
                    seasonalList.add(anime)
                }
            }
            seasonalList.sortByDescending { it.node.mean }
        }
        seasonalRecycler = findViewById(R.id.seasonal_recycler)
        seasonalAdapter = SeasonalAnimeAdapter(
            seasonalList,
            R.layout.list_item_seasonal,
            this,
            onClickListener = {itemView, animeList -> openDetails(animeList.node.id, itemView)})
        seasonalAdapter.setEndListReachedListener(object :EndListReachedListener {
            override fun onBottomReached(position: Int) {
                if (animeResponse!=null) {
                    val nextPage = animeResponse?.paging?.next
                    if (nextPage!=null && nextPage.isNotEmpty()) {
                        val getMoreCall = malApiService.getNextSeasonalPage(nextPage)
                        initCalls(false, getMoreCall)
                    }
                }
            }
        })
        seasonalRecycler.adapter = seasonalAdapter

        filterFab = findViewById(R.id.filter_fab)
        filterFab.addSystemWindowInsetToMargin(bottom = true)
        setupBottomSheet()
        snackBarView = findViewById(R.id.seasonal_layout)
        loadingBar = findViewById(R.id.seasonal_loading)

        val seasonCall = malApiService.getSeasonalAnime(Urls.apiBaseUrl + "anime/season/$year/$season",
            "anime_score", "start_season,broadcast,num_episodes,media_type,mean", 300)
        initCalls(true, seasonCall)
    }
    private fun initCalls(shouldClear: Boolean, call: Call<SeasonalAnimeResponse>) {
        loadingBar.show()
        call.enqueue(object :Callback<SeasonalAnimeResponse> {
            override fun onResponse(
                call: Call<SeasonalAnimeResponse>,
                response: Response<SeasonalAnimeResponse>) {
                if (response.isSuccessful && animeResponse!=response.body()) {
                    animeResponse = response.body()
                    val animeList = animeResponse!!.data
                    animeList.sortByDescending { it.node.mean }
                    if (shouldClear) {
                        seasonalList.clear()
                    }
                    for (anime in animeList) {
                        if (anime.node.start_season==currentSeason) {
                            seasonalList.add(anime)
                        }
                    }
                    animeDb?.seasonalResponseDao()?.insertSeasonalResponse(animeResponse!!)
                    seasonalAdapter.notifyDataSetChanged()
                }
                else if (response.code()==401) {
                    Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
                else if (response.code()==404) {
                    Snackbar.make(snackBarView, getString(R.string.error_not_found), Snackbar.LENGTH_SHORT).show()
                }
                loadingBar.hide()
            }

            override fun onFailure(call: Call<SeasonalAnimeResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                loadingBar.hide()
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_seasonal)
        filterFab.setOnClickListener { bottomSheetDialog.show() }

        val applyButton = bottomSheetDialog.findViewById<Button>(R.id.apply_button)
        applyButton?.setOnClickListener {
            season = StringFormat.formatSeasonInverted(seasonLayout.editText?.text.toString(), this)
            year = yearLayout.editText?.text.toString().toInt()
            val seasonCall = malApiService.getSeasonalAnime(Urls.apiBaseUrl + "anime/season/$year/$season",
                "anime_score", "start_season,broadcast,num_episodes,media_type,mean", 300)
            initCalls(true, seasonCall)
            toolbar.title = "${StringFormat.formatSeason(season, this)} $year"
            bottomSheetDialog.hide()
        }
        val cancelButton = bottomSheetDialog.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener { 
            seasonLayout.editText?.setText(season)
            yearLayout.editText?.setText(year.toString())
            bottomSheetDialog.hide()
        }

        seasonLayout = bottomSheetDialog.findViewById(R.id.season_layout)!!
        val seasons = listOf(getString(R.string.winter), getString(R.string.spring),
            getString(R.string.summer), getString(R.string.fall))
        val seasonAdapter = ArrayAdapter(this, R.layout.list_status_item, seasons)
        (seasonLayout.editText as? AutoCompleteTextView)?.setAdapter(seasonAdapter)
        (seasonLayout.editText as? AutoCompleteTextView)
            ?.setText(StringFormat.formatSeason(season, this), false)

        yearLayout = bottomSheetDialog.findViewById(R.id.year_layout)!!
        val years = mutableListOf<Int>()
        val currentYear = SeasonCalendar.getCurrentYear()
        val baseYear = 1995
        for (x in baseYear..currentYear+1) { years.add(x) }
        years.sortDescending()
        val yearAdapter = ArrayAdapter(this, R.layout.list_status_item, years)
        (yearLayout.editText as? AutoCompleteTextView)?.setAdapter(yearAdapter)
        (yearLayout.editText as? AutoCompleteTextView)?.setText(year.toString(), false)
    }

    private fun openDetails(animeId: Int?, view: View) {
        val poster = view.findViewById<ImageView>(R.id.anime_poster)
        val intent = Intent(this, AnimeDetailsActivity::class.java)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, poster, poster.transitionName)
        intent.putExtra("animeId", animeId)
        startActivity(intent, bundle.toBundle())
    }

    override fun onResume() {
        super.onResume()
        if (seasonalList.isNotEmpty()) {
            loadingBar.hide()
        }
    }
}