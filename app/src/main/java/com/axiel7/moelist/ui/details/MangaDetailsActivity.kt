package com.axiel7.moelist.ui.details

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
import com.axiel7.moelist.model.MangaDetails
import com.axiel7.moelist.model.MyMangaListStatus
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.MainActivity
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

class MangaDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var malApiService: MalApiService
    private lateinit var fields: String
    private lateinit var mangaDetails: MangaDetails
    private lateinit var loadingView: FrameLayout
    private lateinit var editFab: ExtendedFloatingActionButton
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var mangaPosterView: ShapeableImageView
    private lateinit var mangaTitleView: TextView
    private lateinit var mediaTypeView: TextView
    private lateinit var totalChaptersView: TextView
    private lateinit var totalVolumesView: TextView
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
    private lateinit var endDateView: TextView
    private lateinit var sourceView: TextView
    private lateinit var serialView: TextView
    private lateinit var authorsView: TextView
    private lateinit var chaptersLayout: TextInputLayout
    private lateinit var chaptersField: TextInputEditText
    private lateinit var volumesLayout: TextInputLayout
    private lateinit var volumesField: TextInputEditText
    private lateinit var statusLayout: TextInputLayout
    private lateinit var statusField: AutoCompleteTextView
    private lateinit var scoreSlider: Slider
    private var entryUpdated: Boolean = false
    private var retrofit: Retrofit? = null
    private var mangaId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_manga_details)

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

        mangaId = intent.getIntExtra("mangaId", 1)
        fields = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
                "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_chapters,num_volumes," +
                "source,authors{first_name,last_name},serialization"

        initViews()
        setupBottomSheet()
        if (animeDb?.mangaDetailsDao()?.getMangaDetailsById(mangaId)!=null) {
            mangaDetails = animeDb?.mangaDetailsDao()?.getMangaDetailsById(mangaId)!!
            setDataToViews()
        }

        createRetrofitAndApiService()
        initCalls()
    }
    private fun createRetrofitAndApiService() {
        retrofit = if (MainActivity.httpClient !=null) {
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
        val detailsCall = malApiService.getMangaDetails(Urls.apiBaseUrl + "manga/$mangaId", fields)
        initDetailsCall(detailsCall)
    }
    private fun initDetailsCall(call: Call<MangaDetails>?) {
        call?.enqueue(object : Callback<MangaDetails> {
            override fun onResponse(call: Call<MangaDetails>, response: Response<MangaDetails>) {
                Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    mangaDetails = response.body()!!
                    setDataToViews()
                    animeDb?.mangaDetailsDao()?.insertMangaDetails(mangaDetails)
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

            override fun onFailure(call: Call<MangaDetails>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                Toast.makeText(this@MangaDetailsActivity, "Error connecting to server", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
        })
    }
    private fun initUpdateCall(status: String?, score: Int?, chaptersRead: Int?, volumesRead: Int?, newEntry: Boolean) {
        val shouldNotUpdate = status.isNullOrEmpty() && score==null && chaptersRead==null && volumesRead==null
        if (!shouldNotUpdate) {
            val updateListCall = malApiService
                .updateMangaList(Urls.apiBaseUrl + "manga/$mangaId/my_list_status", status, score, chaptersRead, volumesRead)
            patchCall(updateListCall, newEntry)
        } else {
            Toast.makeText(this@MangaDetailsActivity, "No changes", Toast.LENGTH_SHORT).show()
        }
    }
    private fun patchCall(call: Call<MyMangaListStatus>, newEntry: Boolean) {
        call.enqueue(object :Callback<MyMangaListStatus> {
            override fun onResponse(call: Call<MyMangaListStatus>, response: Response<MyMangaListStatus>) {
                if (response.isSuccessful) {
                    val myListStatus = response.body()!!
                    syncListStatus(myListStatus)
                    mangaDetails.my_list_status = myListStatus
                    entryUpdated = true
                    var toastText = "Updated"
                    if (newEntry) {
                        toastText = "Added to Plan to Read"
                    }
                    Toast.makeText(this@MangaDetailsActivity, toastText, Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@MangaDetailsActivity, "Error updating list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyMangaListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Toast.makeText(this@MangaDetailsActivity, "Error updating list", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun deleteEntry() {
        val deleteCall = malApiService.deleteEntry(Urls.apiBaseUrl + "manga/$mangaId/my_list_status")
        deleteCall.enqueue(object :Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    mangaDetails.my_list_status = null
                    changeFabAction()
                    bottomSheetDialog.dismiss()
                    Toast.makeText(this@MangaDetailsActivity, "Deleted", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@MangaDetailsActivity, "Error deleting from list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Toast.makeText(this@MangaDetailsActivity, "Error deleting from list", Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun initViews() {
        loadingView = findViewById(R.id.loading_layout)
        mangaPosterView = findViewById(R.id.manga_poster)
        mangaTitleView = findViewById(R.id.main_title)
        mediaTypeView = findViewById(R.id.media_type_text)
        totalChaptersView = findViewById(R.id.episodes_text)
        totalVolumesView = findViewById(R.id.volumes_text)
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
        endDateView = findViewById(R.id.end_date_text)
        sourceView = findViewById(R.id.source_text)
        serialView = findViewById(R.id.serial_text)
        authorsView = findViewById(R.id.authors_text)
    }
    private fun setupBottomSheet() {
        editFab = findViewById(R.id.edit_fab)
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_edit_manga, null)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setOnDismissListener {
            if (mangaDetails.my_list_status!=null) {
                syncListStatus(mangaDetails.my_list_status!!)
            }
        }

        val applyButton = bottomSheetDialog.findViewById<Button>(R.id.apply_button)
        applyButton?.setOnClickListener {

            var status :String? = null
            val statusCurrent = StringFormat.formatListStatusInverted(statusField.text.toString())
            val statusOrigin = mangaDetails.my_list_status?.status

            var score :Int? = null
            val scoreCurrent = scoreSlider.value.toInt()
            val scoreOrigin = mangaDetails.my_list_status?.score

            var chapters: Int? = null
            val chaptersCurrent = chaptersField.text.toString().toInt()
            val chaptersOrigin = mangaDetails.my_list_status?.num_chapters_read
            var volumes: Int? = null
            val volumesCurrent = volumesField.text.toString().toInt()
            val volumesOrigin = mangaDetails.my_list_status?.num_volumes_read
            if (statusCurrent!=statusOrigin) {
                status = statusCurrent
            }
            if (scoreCurrent!=scoreOrigin) {
                score = scoreCurrent
            }
            if (chaptersCurrent!=chaptersOrigin) {
                chapters = chaptersCurrent
            }
            if (volumesCurrent!=volumesOrigin) {
                volumes = volumesCurrent
            }
            if (status=="completed") {
                chapters = mangaDetails.num_chapters
                volumes = mangaDetails.num_volumes
            }
            initUpdateCall(status, score, chapters, volumes, false)
        }
        val cancelButton = bottomSheetDialog.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            syncListStatus(mangaDetails.my_list_status!!)
            bottomSheetDialog.hide()
        }

        statusLayout = bottomSheetDialog.findViewById(R.id.status_layout)!!
        statusField = bottomSheetDialog.findViewById(R.id.status_field)!!
        val statusItems = listOf("Reading", "Completed", "On Hold", "Dropped", "Plan to Read")
        val adapter = ArrayAdapter(this, R.layout.list_status_item, statusItems)
        (statusLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        chaptersLayout = bottomSheetDialog.findViewById(R.id.chapters_layout)!!
        chaptersField = bottomSheetDialog.findViewById(R.id.chapters_field)!!
        volumesLayout = bottomSheetDialog.findViewById(R.id.volumes_layout)!!
        volumesField = bottomSheetDialog.findViewById(R.id.volumes_field)!!

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
        mangaPosterView.load(mangaDetails.main_picture?.medium) {
            crossfade(true)
            crossfade(300)
            error(R.drawable.ic_launcher_foreground)
        }
        mangaPosterView.setOnClickListener { openFullPoster() }
        mangaTitleView.text = mangaDetails.title

        //media type
        val mediaTypeText = mangaDetails.media_type?.let { StringFormat.formatMediaType(it) }
        mediaTypeView.text = mediaTypeText

        //total episodes
        val numChapters = mangaDetails.num_chapters
        var episodesText = "$numChapters Chapters"
        if (numChapters==0) {
            episodesText = "?? Chapters"
        }
        totalChaptersView.text = episodesText
        // total volumes
        val numVolumes = mangaDetails.num_volumes
        var volumesText = "$numVolumes Volumes"
        if (numVolumes==0) {
            volumesText = "?? Volumes"
        }
        totalVolumesView.text = volumesText

        //media status
        val statusText = mangaDetails.status?.let { StringFormat.formatStatus(it) }
        statusView.text = statusText

        //score and synopsis
        scoreView.text = mangaDetails.mean.toString()
        synopsisView.text = mangaDetails.synopsis

        //genres chips
        val genres = mangaDetails.genres
        if (genres != null) {
            for (genre in genres) {
                val chip = Chip(genresView.context)
                chip.text = genre.name
                genresView.addView(chip)
            }
        }

        //stats
        val topRank = mangaDetails.rank
        val rankText = "#$topRank"
        rankView.text = if (topRank==null) { "N/A" } else { rankText }

        val membersRank = mangaDetails.num_scoring_users
        numScoresView.text = NumberFormat.getInstance().format(membersRank)

        membersView.text = NumberFormat.getInstance().format(mangaDetails.num_list_users)

        val popularity = mangaDetails.popularity
        val popularityText = "#$popularity"
        popularityView.text = popularityText

        //more info
        val synonyms = mangaDetails.alternative_titles?.synonyms
        val synonymsText = synonyms?.joinToString(separator = ",\n")
        synonymsView.text = if (!synonymsText.isNullOrEmpty()) { synonymsText }
        else { "─" }

        jpTitleView.text = if (!mangaDetails.alternative_titles?.ja.isNullOrEmpty()) {
            mangaDetails.alternative_titles?.ja }
        else { "─" }

        startDateView.text = if (!mangaDetails.start_date.isNullOrEmpty()) { mangaDetails.start_date }
        else { "Unknown" }
        endDateView.text = if (!mangaDetails.end_date.isNullOrEmpty()) { mangaDetails.end_date }
        else { "Unknown" }

        // authors
        val authors = mangaDetails.authors
        val authorsRoles = mutableListOf<String>()
        val authorsNames = mutableListOf<String>()
        val authorsSurnames = mutableListOf<String>()
        val authorsText = mutableListOf<String>()
        if (authors != null) {
            for (author in authors) {
                authorsRoles.add(author.role)
                authorsNames.add(author.node.first_name)
                authorsSurnames.add(author.node.last_name)
            }
            for (index in authors.indices) {
                authorsText.add("${authorsNames[index]} ${authorsSurnames[index]} (${authorsRoles[index]})")
            }
        }

        val authorsTextFormatted = authorsText.joinToString(separator = ",\n")
        authorsView.text = authorsTextFormatted

        // serialization
        val serialization = mangaDetails.serialization
        val serialNames = mutableListOf<String>()
        if (serialization != null) {
            for (serial in serialization) {
                serialNames.add(serial.node.name)
            }
        }
        val serialText = serialNames.joinToString(separator = ",\n")
        serialView.text = serialText

        //bottom sheet edit
        chaptersLayout.suffixText = "/$numChapters"
        volumesLayout.suffixText = "/$numVolumes"
        if (mangaDetails.my_list_status!=null) {
            syncListStatus(mangaDetails.my_list_status!!)
        }
        // chapters/volumes input logic
        if (numChapters!=0) {
            chaptersLayout.editText?.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty() && text.isNotBlank()
                    && text.toString().toInt() > numChapters!!) {
                    chaptersLayout.error = "Invalid number"
                } else { chaptersLayout.error = null }
            }
        }
        if (numVolumes!=0) {
            volumesLayout.editText?.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty() && text.isNotBlank()
                    && text.toString().toInt() > numVolumes!!) {
                    volumesLayout.error = "Invalid number"
                } else { volumesLayout.error = null }
            }
        }
    }
    private fun changeFabAction() {
        //change fab behavior if not added
        if (mangaDetails.my_list_status==null) {
            editFab.text = "Add"
            editFab.setIconResource(R.drawable.ic_round_add_24)
            editFab.setOnClickListener {
                initUpdateCall("plan_to_read", null, null, null, true)
                editFab.text = "Edit"
                editFab.setIconResource(R.drawable.ic_round_edit_24)
                val bottomSheetBehavior = bottomSheetDialog.behavior
                editFab.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    bottomSheetBehavior.peekHeight = 1150
                    bottomSheetDialog.show()
                }
            }
        } else {
            editFab.text = "Edit"
            editFab.setIconResource(R.drawable.ic_round_edit_24)
            val bottomSheetBehavior = bottomSheetDialog.behavior
            editFab.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.peekHeight = 1150
                bottomSheetDialog.show()
            }
        }
    }
    private fun syncListStatus(myListStatus: MyMangaListStatus) {
        val chaptersRead = myListStatus.num_chapters_read
        chaptersField.setText(chaptersRead.toString())
        val volumesRead = myListStatus.num_volumes_read
        volumesField.setText(volumesRead.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus.status)
        statusField.setText(statusValue, false)

        scoreSlider.value = myListStatus.score.toFloat()
    }

    private fun openFullPoster() {
        val intent = Intent(this, FullPosterActivity::class.java)
        val largePicture = mangaDetails.main_picture?.large
        val mediumPicture = mangaDetails.main_picture?.medium
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