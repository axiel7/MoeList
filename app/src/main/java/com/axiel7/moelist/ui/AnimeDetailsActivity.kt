package com.axiel7.moelist.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import coil.api.load
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.R
import com.axiel7.moelist.model.AnimeDetails
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import okhttp3.Cache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AnimeDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var malApiService: MalApiService
    private lateinit var fields: String
    private lateinit var animeDetails: AnimeDetails
    private lateinit var loadingView: FrameLayout
    private lateinit var animePosterView: ShapeableImageView
    private lateinit var animeTitleView: TextView
    private lateinit var mediaTypeView: TextView
    private lateinit var totalEpisodesView: TextView
    private lateinit var statusView: TextView
    private lateinit var scoreView: TextView
    private lateinit var genresView: ChipGroup
    private lateinit var synopsisView: TextView
    private lateinit var rankView: TextView
    private lateinit var membersView: TextView
    private lateinit var numScoresView: TextView
    private lateinit var popularityView: TextView
    private lateinit var synonymsView: TextView
    private lateinit var jpTitleView: TextView
    private lateinit var startDateView: TextView
    private lateinit var seasonView: TextView
    private lateinit var durationView: TextView
    private lateinit var sourceView: TextView
    private lateinit var studiosView: TextView
    private var retrofit: Retrofit? = null
    private var cache: Cache? = null
    private var animeId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_anime_details)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
        val toolbar = findViewById<Toolbar>(R.id.details_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        SharedPrefsHelpers.init(this)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        animeId = intent.getIntExtra("animeId", 1)
        fields = "id,title,main_picture,alternative_titles,start_date,synopsis,mean,rank,popularity," +
                "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_episodes,start_season," +
                "source,average_episode_duration,studios"

        initViews()
        setupBottomSheet()
        if (animeDb?.animeDetailsDao()?.getAnimeDetailsById(animeId)!=null) {
            animeDetails = animeDb?.animeDetailsDao()?.getAnimeDetailsById(animeId)!!
            setDataToViews()
        }

        connectAndGetApiData()

    }
    private fun connectAndGetApiData() {
        if (retrofit==null) {
            retrofit = Retrofit.Builder()
                .baseUrl(Urls.apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(CreateOkHttpClient.createOkHttpClient(accessToken, this, true))
                .build()
        }

        malApiService = retrofit?.create(MalApiService::class.java)!!
        val detailsCall = malApiService.getAnimeDetails(Urls.apiBaseUrl + "anime/$animeId", fields)
        initDetailsCall(detailsCall)
    }
    private fun initDetailsCall(call: Call<AnimeDetails>?) {
        call?.enqueue(object : Callback<AnimeDetails> {
            override fun onResponse(call: Call<AnimeDetails>, response: Response<AnimeDetails>) {
                Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    animeDetails = response.body()!!
                    setDataToViews()
                    animeDb?.animeDetailsDao()?.insertAnimeDetails(animeDetails)
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

            override fun onFailure(call: Call<AnimeDetails>, t: Throwable) {
                Log.e("MoeLog", t.toString())
            }
        })
    }
    private fun initViews() {
        loadingView = findViewById(R.id.loading_layout)
        animePosterView = findViewById(R.id.anime_poster)
        animeTitleView = findViewById(R.id.main_title)
        mediaTypeView = findViewById(R.id.media_type_text)
        totalEpisodesView = findViewById(R.id.episodes_text)
        statusView = findViewById(R.id.status_text)
        scoreView = findViewById(R.id.score_text)
        genresView = findViewById(R.id.chip_group_genres)

        synopsisView = findViewById(R.id.synopsis)
        val synopsisIcon = findViewById<ImageView>(R.id.synopsis_icon)
        synopsisIcon.setOnClickListener {
            if (synopsisView.maxLines==5) {
                synopsisView.maxLines = Int.MAX_VALUE
                synopsisIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_arrow_up_24))
            }
            else {
                synopsisView.maxLines = 5
                synopsisIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_arrow_down_24))
            }
        }

        rankView = findViewById(R.id.rank_text)
        membersView = findViewById(R.id.members_text)
        numScoresView = findViewById(R.id.num_scores_text)
        popularityView = findViewById(R.id.popularity_text)
        synonymsView = findViewById(R.id.synonyms_text)
        jpTitleView = findViewById(R.id.jp_title)
        startDateView = findViewById(R.id.start_date_text)
        seasonView = findViewById(R.id.season_text)
        durationView = findViewById(R.id.duration_text)
        sourceView = findViewById(R.id.source_text)
        studiosView = findViewById(R.id.studios_text)
    }
    override fun onDestroy() {
        super.onDestroy()
        cache?.flush()
    }
}