package com.axiel7.moelist.ui.charts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.SeasonalAnimeAdapter
import com.axiel7.moelist.model.SeasonalAnimeResponse
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.MainActivity
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.utils.*
import com.axiel7.moelist.utils.InsetsHelper.addSystemWindowInsetToMargin
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class SeasonalActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var malApiService: MalApiService
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
    private var year by Delegates.notNull<Int>()
    private var animeResponse: SeasonalAnimeResponse? = null
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seasonal)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorCard)

        toolbar = findViewById(R.id.seasonal_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        SharedPrefsHelpers.init(this)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        season = SeasonCalendar.getCurrentSeason()
        year = SeasonCalendar.getCurrentYear()
        toolbar.title = "${StringFormat.formatSeason(season, this)} $year"

        val savedResponse = animeDb?.seasonalResponseDao()?.getSeasonalResponse(season, year)
        if (savedResponse!=null) {
            animeResponse = savedResponse
            seasonalList = animeResponse!!.data
            seasonalList.sortByDescending { it.node.mean }
        }
        else { seasonalList = mutableListOf() }
        seasonalRecycler = findViewById(R.id.seasonal_recycler)
        seasonalAdapter = SeasonalAnimeAdapter(
            seasonalList,
            R.layout.list_item_seasonal,
            this,
            onClickListener = {itemView, animeList -> openDetails(animeList.node.id, itemView)})
        seasonalRecycler.adapter = seasonalAdapter

        filterFab = findViewById(R.id.filter_fab)
        filterFab.addSystemWindowInsetToMargin(bottom = true)
        setupBottomSheet()
        snackBarView = findViewById(R.id.seasonal_layout)
        loadingBar = findViewById(R.id.seasonal_loading)

        createRetrofitAndApiService()
        initCalls(false)
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
                .client(CreateOkHttpClient.createOkHttpClient(this, true))
                .build()
        }
        malApiService = retrofit?.create(MalApiService::class.java)!!
    }
    private fun initCalls(shouldClear: Boolean) {
        loadingBar.show()
        val seasonCall = malApiService.getSeasonalAnime(Urls.apiBaseUrl + "anime/season/$year/$season",
            "anime_score", "broadcast,num_episodes,media_type,mean", 300)
        seasonCall.enqueue(object :Callback<SeasonalAnimeResponse> {
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
                    seasonalList.addAll(animeList)
                    animeDb?.seasonalResponseDao()?.insertSeasonalResponse(animeResponse!!)
                    seasonalAdapter.notifyDataSetChanged()
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
            initCalls(true)
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