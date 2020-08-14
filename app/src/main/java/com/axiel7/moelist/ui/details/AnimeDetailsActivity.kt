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
import androidx.core.widget.doOnTextChanged
import coil.api.load
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.R
import com.axiel7.moelist.model.AnimeDetails
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat

class AnimeDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var malApiService: MalApiService
    private lateinit var fields: String
    private lateinit var animeDetails: AnimeDetails
    private lateinit var loadingView: FrameLayout
    private lateinit var editFab: ExtendedFloatingActionButton
    private lateinit var bottomSheetDialog: BottomSheetDialog
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
    private lateinit var episodesLayout: TextInputLayout
    private lateinit var episodesField: TextInputEditText
    private lateinit var statusLayout: TextInputLayout
    private lateinit var statusField: AutoCompleteTextView
    private lateinit var scoreSlider: Slider
    private var entryUpdated: Boolean = false
    private var retrofit: Retrofit? = null
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

        createRetrofitAndApiService()
        initCalls()
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
    private fun initCalls() {
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
                Toast.makeText(this@AnimeDetailsActivity, "Error connecting to server", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
        })
    }
    private fun initUpdateCall(status: String?, score: Int?, watchedEpisodes: Int?, newEntry: Boolean) {
        val shouldNotUpdate = status.isNullOrEmpty() && score==null && watchedEpisodes==null
        if (!shouldNotUpdate) {
            val updateListCall = malApiService
                .updateAnimeList(Urls.apiBaseUrl + "anime/$animeId/my_list_status", status, score, watchedEpisodes)
            patchCall(updateListCall, newEntry)
        } else {
            Toast.makeText(this@AnimeDetailsActivity, "No changes", Toast.LENGTH_SHORT).show()
        }
    }
    private fun patchCall(call: Call<MyListStatus>, newEntry: Boolean) {
        call.enqueue(object :Callback<MyListStatus> {
            override fun onResponse(call: Call<MyListStatus>, response: Response<MyListStatus>) {
                if (response.isSuccessful) {
                    val myListStatus = response.body()!!
                    syncListStatus(myListStatus)
                    animeDetails.my_list_status = myListStatus
                    entryUpdated = true
                    var toastText = "Updated"
                    if (newEntry) {
                        toastText = "Added to Plan to Watch"
                    }
                    Toast.makeText(this@AnimeDetailsActivity, toastText, Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@AnimeDetailsActivity, "Error updating list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Toast.makeText(this@AnimeDetailsActivity, "Error updating list", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun deleteEntry() {
        val deleteCall = malApiService.deleteEntry(Urls.apiBaseUrl + "anime/$animeId/my_list_status")
        deleteCall.enqueue(object :Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    animeDetails.my_list_status = null
                    changeFabAction()
                    bottomSheetDialog.dismiss()
                    Toast.makeText(this@AnimeDetailsActivity, "Deleted", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@AnimeDetailsActivity, "Error deleting from list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Toast.makeText(this@AnimeDetailsActivity, "Error deleting from list", Toast.LENGTH_SHORT).show()
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
    private fun setupBottomSheet() {
        editFab = findViewById(R.id.edit_fab)
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_edit_anime, null)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setOnDismissListener {
            if (animeDetails.my_list_status!=null) {
                syncListStatus(animeDetails.my_list_status!!)
            }
        }

        /*val scrollView: NestedScrollView? = bottomSheetDialog.findViewById(R.id.details_scroll)
        scrollView?.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                editFab.shrink()
            } else {
                editFab.extend()
            }
        }*/

        val applyButton = bottomSheetDialog.findViewById<Button>(R.id.apply_button)
        applyButton?.setOnClickListener {

            var status :String? = null
            val statusCurrent = StringFormat.formatListStatusInverted(statusField.text.toString())
            val statusOrigin = animeDetails.my_list_status?.status

            var score :Int? = null
            val scoreCurrent = scoreSlider.value.toInt()
            val scoreOrigin = animeDetails.my_list_status?.score

            var episodes: Int? = null
            val episodesCurrent = episodesField.text.toString().toInt()
            val episodesOrigin = animeDetails.my_list_status?.num_episodes_watched
            if (statusCurrent!=statusOrigin) {
                status = statusCurrent
            }
            if (scoreCurrent!=scoreOrigin) {
                score = scoreCurrent
            }
            if (episodesCurrent!=episodesOrigin) {
                episodes = episodesCurrent
            }
            initUpdateCall(status, score, episodes, false)
        }
        val cancelButton = bottomSheetDialog.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            syncListStatus(animeDetails.my_list_status!!)
            bottomSheetDialog.hide()
        }

        statusLayout = bottomSheetDialog.findViewById(R.id.status_layout)!!
        statusField = bottomSheetDialog.findViewById(R.id.status_field)!!
        val statusItems = listOf("Watching", "Completed", "On Hold", "Dropped", "Plan to Watch")
        val adapter = ArrayAdapter(this, R.layout.list_status_item, statusItems)
        (statusLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        episodesLayout = bottomSheetDialog.findViewById(R.id.episodes_field_layout)!!
        episodesField = bottomSheetDialog.findViewById(R.id.episodes_field)!!

        val scoreText = bottomSheetDialog.findViewById<TextView>(R.id.score_text)
        scoreSlider = bottomSheetDialog.findViewById(R.id.score_slider)!!
        scoreSlider.addOnChangeListener { _, value, _ ->
            val scoreTextValue = "Score: " + value.toInt().let { StringFormat.formatScore(it) }
            scoreText?.text = scoreTextValue
        }
        val scoreTextValue = "Score: " + scoreSlider.value.toInt().let { StringFormat.formatScore(it) }
        scoreText?.text = scoreTextValue

        val deleteButton = bottomSheetDialog.findViewById<Button>(R.id.delete_button)
        deleteButton?.setOnClickListener { deleteEntry() }
    }
    private fun setDataToViews() {
        //quit loading bar
        loadingView.visibility = View.GONE

        changeFabAction()

        //poster and main title
        animePosterView.load(animeDetails.main_picture?.medium)
        animePosterView.setOnClickListener { openFullPoster() }
        animeTitleView.text = animeDetails.title

        //media type
        val mediaTypeText = animeDetails.media_type?.let { StringFormat.formatMediaType(it) }
        mediaTypeView.text = mediaTypeText

        //total episodes
        val numEpisodes = animeDetails.num_episodes
        var episodesText = "$numEpisodes Episodes"
        if (numEpisodes==0) {
            episodesText = "?? Episodes"
        }
        totalEpisodesView.text = episodesText

        //media status
        val statusText = animeDetails.status?.let { StringFormat.formatStatus(it) }
        statusView.text = statusText

        //score and synopsis
        scoreView.text = animeDetails.mean.toString()
        synopsisView.text = animeDetails.synopsis

        //genres chips
        val genres = animeDetails.genres
        if (genres != null) {
            for (genre in genres) {
                val chip = Chip(genresView.context)
                chip.text = genre.name
                genresView.addView(chip)
            }
        }

        //stats
        val topRank = animeDetails.rank
        val rankText = "#$topRank"
        rankView.text = if (topRank==null) { "N/A" } else { rankText }

        val membersRank = animeDetails.num_scoring_users
        numScoresView.text = NumberFormat.getInstance().format(membersRank)

        membersView.text = NumberFormat.getInstance().format(animeDetails.num_list_users)

        val popularity = animeDetails.popularity
        val popularityText = "#$popularity"
        popularityView.text = popularityText

        //more info
        val synonyms = animeDetails.alternative_titles?.synonyms
        val synonymsText = synonyms?.joinToString(separator = ",\n")
        synonymsView.text = if (!synonymsText.isNullOrEmpty()) { synonymsText }
        else { "─" }

        jpTitleView.text = animeDetails.alternative_titles?.ja
        jpTitleView.text = if (!animeDetails.alternative_titles?.ja.isNullOrEmpty()) {
            animeDetails.alternative_titles?.ja }
        else { "─" }

        startDateView.text = if (!animeDetails.start_date.isNullOrEmpty()) { animeDetails.start_date }
        else { "Unknown" }

        val year = animeDetails.start_season?.year
        var season = animeDetails.start_season?.season
        season = season?.let { StringFormat.formatSeason(it) }
        val seasonText = "$season $year"
        seasonView.text = if (animeDetails.start_season!=null) { seasonText }
        else { "Unknown" }

        val duration = animeDetails.average_episode_duration?.div(60)
        val durationText = "$duration min."
        durationView.text = if (duration==0) { "Unknown" } else { durationText }

        var sourceText =  animeDetails.source
        sourceText = sourceText?.let { StringFormat.formatSource(it) }
        sourceView.text = sourceText

        val studios = animeDetails.studios
        val studiosNames = mutableListOf<String>()
        if (studios != null) {
            for (studio in studios) {
                studiosNames.add(studio.name)
            }
        }
        val studiosText = studiosNames.joinToString(separator = ",\n")
        studiosView.text = studiosText

        //bottom sheet edit
        episodesLayout.suffixText = "/$numEpisodes"
        if (animeDetails.my_list_status!=null) {
            syncListStatus(animeDetails.my_list_status!!)
        }
        //episodes input logic
        if (numEpisodes!=0) {
            episodesLayout.editText?.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty() && text.isNotBlank()
                    && text.toString().toInt() > numEpisodes!!) {
                    episodesLayout.error = "Invalid number"
                } else { episodesLayout.error = null }
            }
        }
    }
    private fun changeFabAction() {
        //change fab behavior if not added
        if (animeDetails.my_list_status==null) {
            editFab.text = "Add"
            editFab.setIconResource(R.drawable.ic_round_add_24)
            editFab.setOnClickListener {
                initUpdateCall("plan_to_watch", null, null, true)
                editFab.text = "Edit"
                editFab.setIconResource(R.drawable.ic_round_edit_24)
                val bottomSheetBehavior = bottomSheetDialog.behavior
                editFab.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    bottomSheetBehavior.peekHeight = 900
                    bottomSheetDialog.show()
                }
            }
        } else {
            editFab.text = "Edit"
            editFab.setIconResource(R.drawable.ic_round_edit_24)
            val bottomSheetBehavior = bottomSheetDialog.behavior
            editFab.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.peekHeight = 900
                bottomSheetDialog.show()
            }
        }
    }
    private fun syncListStatus(myListStatus: MyListStatus) {
        val watchedEpisodes = myListStatus.num_episodes_watched
        episodesField.setText(watchedEpisodes.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus.status)
        statusField.setText(statusValue, false)

        scoreSlider.value = myListStatus.score.toFloat()
    }

    private fun openFullPoster() {
        val intent = Intent(this, FullPosterActivity::class.java)
        val largePicture = animeDetails.main_picture?.large
        val mediumPicture = animeDetails.main_picture?.medium
        if (!largePicture.isNullOrEmpty()) {
            intent.putExtra("posterUrl", largePicture)
            startActivity(intent)
        }
        else if (!mediumPicture.isNullOrEmpty()) {
            intent.putExtra("posterUrl", mediumPicture)
            startActivity(intent)
        }
    }

    override fun finish() {
        val returnIntent = Intent()
        returnIntent.putExtra("entryUpdated", entryUpdated)
        setResult(Activity.RESULT_OK, returnIntent)
        super.finish()
    }
}