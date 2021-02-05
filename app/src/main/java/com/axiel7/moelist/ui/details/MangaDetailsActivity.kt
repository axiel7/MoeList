package com.axiel7.moelist.ui.details

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.RelatedsAdapter
import com.axiel7.moelist.model.MangaDetails
import com.axiel7.moelist.model.MyMangaListStatus
import com.axiel7.moelist.model.Related
import com.axiel7.moelist.ui.BaseActivity
import com.axiel7.moelist.utils.*
import com.axiel7.moelist.utils.InsetsHelper.getViewBottomHeight
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class MangaDetailsActivity : BaseActivity() {

    private lateinit var fields: String
    private lateinit var mangaDetails: MangaDetails
    private lateinit var loadingView: FrameLayout
    private lateinit var editFab: ExtendedFloatingActionButton
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dialogView: View
    private lateinit var mangaPosterView: ShapeableImageView
    private lateinit var mangaTitleView: TextView
    private lateinit var mediaTypeView: TextView
    private lateinit var totalChaptersView: TextView
    private lateinit var totalVolumesView: TextView
    private lateinit var statusView: TextView
    private lateinit var scoreView: TextView
    private lateinit var genresView: ChipGroup
    private lateinit var translateButton: TextView
    private lateinit var loadingTranslate: ContentLoadingProgressBar
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
    private lateinit var relatedRecycler: RecyclerView
    private lateinit var relatedsAdapter: RelatedsAdapter
    private lateinit var chaptersLayout: TextInputLayout
    private lateinit var chaptersField: TextInputEditText
    private lateinit var volumesLayout: TextInputLayout
    private lateinit var volumesField: TextInputEditText
    private lateinit var statusLayout: TextInputLayout
    private lateinit var statusField: AutoCompleteTextView
    private lateinit var scoreSlider: Slider
    private lateinit var snackBarView: View
    private val relateds: MutableList<Related> = mutableListOf()
    private var entryUpdated: Boolean = false
    private var mangaId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manga_details)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val toolbar = findViewById<Toolbar>(R.id.details_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val data = intent?.dataString
        mangaId = if (data?.startsWith("https://myanimelist.net/manga") == true) {
            data.split("/")[4].toIntOrNull() ?: 1
        } else {
            intent.getIntExtra("mangaId", 1)
        }
        fields = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
                "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_chapters,num_volumes," +
                "source,authors{first_name,last_name},serialization,related_anime{media_type},related_manga{media_type}"

        snackBarView = findViewById(R.id.details_layout)
        initViews()
        setupBottomSheet()
        if (animeDb?.mangaDetailsDao()?.getMangaDetailsById(mangaId)!=null) {
            mangaDetails = animeDb?.mangaDetailsDao()?.getMangaDetailsById(mangaId)!!
            setDataToViews()
        }

        initCalls()
    }
    private fun initCalls() {
        val detailsCall = malApiService.getMangaDetails(Urls.apiBaseUrl + "manga/$mangaId", fields)
        initDetailsCall(detailsCall)
    }
    private fun initDetailsCall(call: Call<MangaDetails>?) {
        call?.enqueue(object : Callback<MangaDetails> {
            override fun onResponse(call: Call<MangaDetails>, response: Response<MangaDetails>) {
                if (response.isSuccessful) {
                    mangaDetails = response.body()!!
                    setDataToViews()
                    animeDb?.mangaDetailsDao()?.insertMangaDetails(mangaDetails)
                }
                else if (response.code()==401) {
                    Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MangaDetails>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
            Snackbar.make(snackBarView, getString(R.string.no_changes), Snackbar.LENGTH_SHORT).show()
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
                    var toastText = getString(R.string.updated)
                    if (newEntry) {
                        toastText = getString(R.string.added_ptr)
                    }
                    Snackbar.make(snackBarView, toastText, Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(snackBarView, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyMangaListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
                    Snackbar.make(snackBarView, getString(R.string.deleted), Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(snackBarView, getString(R.string.error_delete_entry), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }

        })
    }
    private fun initViews() {
        loadingView = findViewById(R.id.loading_layout)
        mangaPosterView = findViewById(R.id.manga_poster)
        mangaTitleView = findViewById(R.id.main_title)
        mangaTitleView.setOnLongClickListener {
            copyToClipboard(mangaTitleView.text.toString())
            true
        }
        mediaTypeView = findViewById(R.id.media_type_text)
        totalChaptersView = findViewById(R.id.episodes_text)
        totalVolumesView = findViewById(R.id.volumes_text)
        statusView = findViewById(R.id.status_text)
        scoreView = findViewById(R.id.score_text)
        genresView = findViewById(R.id.chip_group_genres)

        translateButton = findViewById(R.id.translate_button)
        loadingTranslate = findViewById(R.id.loading_translate)
        loadingTranslate.hide()
        if (Locale.getDefault().language == "en") {
            translateButton.visibility = View.GONE
        }
        else {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(Locale.getDefault().language)!!)
                .build()
            val translator = Translation.getClient(options)
            lifecycle.addObserver(translator)
            translateButton.setOnClickListener {
                if (synopsisView.text == mangaDetails.synopsis) {
                    loadingTranslate.show()
                    translateSynopsis(translator)
                }
                else {
                    translateButton.text = resources.getString(R.string.translate)
                    synopsisView.text = mangaDetails.synopsis
                }
            }
        }

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
        TooltipCompat.setTooltipText(rankView, getString(R.string.top_ranked))
        membersView = findViewById(R.id.members_text)
        TooltipCompat.setTooltipText(membersView, getString(R.string.members))
        numScoresView = findViewById(R.id.num_scores_text)
        TooltipCompat.setTooltipText(numScoresView, getString(R.string.users_scores))
        popularityView = findViewById(R.id.popularity_text)
        TooltipCompat.setTooltipText(popularityView, getString(R.string.popularity))
        synonymsView = findViewById(R.id.synonyms_text)
        jpTitleView = findViewById(R.id.jp_title)
        startDateView = findViewById(R.id.start_date_text)
        endDateView = findViewById(R.id.end_date_text)
        sourceView = findViewById(R.id.source_text)
        serialView = findViewById(R.id.serial_text)
        authorsView = findViewById(R.id.authors_text)

        relatedRecycler = findViewById(R.id.relateds_recycler)
        relatedsAdapter = RelatedsAdapter(
            relateds,
            R.layout.list_item_anime_related,
            applicationContext,
            onClickListener = { _, related -> openDetails(related.node.id, related.node.media_type)} )
        relatedRecycler.adapter = relatedsAdapter
    }
    @SuppressLint("InflateParams")
    private fun setupBottomSheet() {
        editFab = findViewById(R.id.edit_fab)
        //editFab.addSystemWindowInsetToMargin(bottom = true)
        dialogView = layoutInflater.inflate(R.layout.bottom_sheet_edit_manga, null)
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
            val statusCurrent = StringFormat.formatListStatusInverted(statusField.text.toString(), this)
            val statusOrigin = mangaDetails.my_list_status?.status

            var score :Int? = null
            val scoreCurrent = scoreSlider.value.toInt()
            val scoreOrigin = mangaDetails.my_list_status?.score

            var chapters: Int? = null
            val chaptersCurrent = chaptersField.text.toString().toIntOrNull()
            val chaptersOrigin = mangaDetails.my_list_status?.num_chapters_read
            var volumes: Int? = null
            val volumesCurrent = volumesField.text.toString().toIntOrNull()
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
            bottomSheetDialog.hide()
        }
        val cancelButton = bottomSheetDialog.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            syncListStatus(mangaDetails.my_list_status!!)
            bottomSheetDialog.hide()
        }

        statusLayout = bottomSheetDialog.findViewById(R.id.status_layout)!!
        statusField = bottomSheetDialog.findViewById(R.id.status_field)!!
        val statusItems = listOf(getString(R.string.reading), getString(R.string.completed),
            getString(R.string.on_hold), getString(R.string.dropped), getString(R.string.ptr))
        val adapter = ArrayAdapter(this, R.layout.list_status_item, statusItems)
        (statusLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        chaptersLayout = bottomSheetDialog.findViewById(R.id.chapters_layout)!!
        chaptersField = bottomSheetDialog.findViewById(R.id.chapters_field)!!
        volumesLayout = bottomSheetDialog.findViewById(R.id.volumes_layout)!!
        volumesField = bottomSheetDialog.findViewById(R.id.volumes_field)!!

        val scoreText = bottomSheetDialog.findViewById<TextView>(R.id.score_text)
        scoreSlider = bottomSheetDialog.findViewById(R.id.score_slider)!!
        scoreSlider.addOnChangeListener { _, value, _ ->
            val scoreTextValue = "${getString(R.string.score_value)} " +
                    value.toInt().let { StringFormat.formatScore(it, this) }
            scoreText?.text = scoreTextValue
        }
        val scoreTextValue = "${getString(R.string.score_value)} " +
                scoreSlider.value.toInt().let { StringFormat.formatScore(it, this) }
        scoreText?.text = scoreTextValue

        val deleteButton = bottomSheetDialog.findViewById<Button>(R.id.delete_button)
        deleteButton?.setOnClickListener { deleteEntry() }
    }
    private fun setDataToViews() {
        val unknown = getString(R.string.unknown)
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
        val mediaTypeText = mangaDetails.media_type?.let { StringFormat.formatMediaType(it, this) }
        mediaTypeView.text = mediaTypeText

        //total episodes
        val numChapters = mangaDetails.num_chapters
        var episodesText = "$numChapters ${getString(R.string.chapters)}"
        if (numChapters==0) {
            episodesText = "?? ${getString(R.string.chapters)}"
        }
        totalChaptersView.text = episodesText
        // total volumes
        val numVolumes = mangaDetails.num_volumes
        var volumesText = "$numVolumes ${getString(R.string.volumes)}"
        if (numVolumes==0) {
            volumesText = "?? ${getString(R.string.volumes)}"
        }
        totalVolumesView.text = volumesText

        //media status
        val statusText = mangaDetails.status?.let { StringFormat.formatStatus(it, this) }
        statusView.text = statusText

        //score and synopsis
        scoreView.text = mangaDetails.mean.toString()
        synopsisView.text = mangaDetails.synopsis

        //genres chips
        val genres = mangaDetails.genres
        if (genres != null && genresView.childCount==0) {
            for (genre in genres) {
                val chip = Chip(genresView.context)
                chip.text = StringFormat.formatGenre(genre.name, applicationContext)
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
        else { unknown }
        endDateView.text = if (!mangaDetails.end_date.isNullOrEmpty()) { mangaDetails.end_date }
        else { unknown }

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
        authorsView.text = if (authorsTextFormatted.isNotEmpty()) { authorsTextFormatted }
        else { unknown }

        // serialization
        val serialization = mangaDetails.serialization
        val serialNames = mutableListOf<String>()
        if (serialization != null) {
            for (serial in serialization) {
                serialNames.add(serial.node.name)
            }
        }
        val serialText = serialNames.joinToString(separator = ",\n")
        serialView.text = if (serialText.isNotEmpty()) { serialText }
        else { unknown }

        //relateds
        val relatedAnimes = mangaDetails.related_anime
        val relatedMangas = mangaDetails.related_manga
        if (relatedAnimes != null && !relateds.containsAll(relatedAnimes)) {
            relateds.addAll(relatedAnimes)
        }
        if (relatedMangas != null && !relateds.containsAll(relatedMangas)) {
            relateds.addAll(relatedMangas)
        }
        relatedsAdapter.notifyDataSetChanged()

        //bottom sheet edit
        chaptersLayout.suffixText = "/$numChapters"
        volumesLayout.suffixText = "/$numVolumes"
        if (mangaDetails.my_list_status!=null) {
            syncListStatus(mangaDetails.my_list_status!!)
        }
        // chapters/volumes input logic
        chaptersLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputChapters = text.toString().toIntOrNull()
            if (numChapters!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputChapters==null
                    || inputChapters > numChapters!!) {
                    chaptersLayout.error = getString(R.string.invalid_number)
                } else { chaptersLayout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    chaptersLayout.error = getString(R.string.invalid_number)
                } else { chaptersLayout.error = null }
            }
        }
        volumesLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputVolumes = text.toString().toIntOrNull()
            if (numVolumes!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputVolumes==null
                    || inputVolumes > numVolumes!!) {
                    volumesLayout.error = getString(R.string.invalid_number)
                } else { volumesLayout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    volumesLayout.error = getString(R.string.invalid_number)
                } else { volumesLayout.error = null }
            }
        }
    }
    private fun changeFabAction() {
        //change fab behavior if not added
        if (mangaDetails.my_list_status==null) {
            editFab.text = getString(R.string.add)
            editFab.setIconResource(R.drawable.ic_round_add_24)
            editFab.setOnClickListener {
                initUpdateCall("plan_to_read", null, null, null, true)
                editFab.text = getString(R.string.edit)
                editFab.setIconResource(R.drawable.ic_round_edit_24)
                val bottomSheetBehavior = bottomSheetDialog.behavior
                editFab.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    getViewBottomHeight(dialogView as ViewGroup, R.id.divider, bottomSheetBehavior)
                    bottomSheetDialog.show()
                }
            }
        } else {
            editFab.text = getString(R.string.edit)
            editFab.setIconResource(R.drawable.ic_round_edit_24)
            val bottomSheetBehavior = bottomSheetDialog.behavior
            editFab.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                getViewBottomHeight(dialogView as ViewGroup, R.id.divider, bottomSheetBehavior)
                bottomSheetDialog.show()
            }
        }
    }
    private fun syncListStatus(myListStatus: MyMangaListStatus) {
        val chaptersRead = myListStatus.num_chapters_read
        chaptersField.setText(chaptersRead.toString())
        val volumesRead = myListStatus.num_volumes_read
        volumesField.setText(volumesRead.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus.status, this)
        statusField.setText(statusValue, false)

        scoreSlider.value = myListStatus.score.toFloat()
    }

    private fun openFullPoster() {
        val intent = Intent(this, FullPosterActivity::class.java)
        val options = ActivityOptions.makeSceneTransitionAnimation(
            this,
            mangaPosterView,
            mangaPosterView.transitionName
        )
        val largePicture = mangaDetails.main_picture?.large
        val mediumPicture = mangaDetails.main_picture?.medium
        if (!largePicture.isNullOrEmpty()) {
            intent.putExtra("posterUrl", largePicture)
            startActivity(intent, options.toBundle())
        }
        else if (!mediumPicture.isNullOrEmpty()) {
            intent.putExtra("posterUrl", mediumPicture)
            startActivity(intent, options.toBundle())
        }
    }
    private fun openDetails(id: Int, mediaType: String?) {
        if (!mediaType.isNullOrEmpty()) {
            if (mediaType=="manga" || mediaType=="one_shot" || mediaType=="manhwa"
                || mediaType=="novel" || mediaType=="doujinshi" || mediaType=="light_novel"
                || mediaType=="manhua") {
                val intent = Intent(this, MangaDetailsActivity::class.java)
                intent.putExtra("mangaId", id)
                intent.putExtra("defaultTransition", true)
                startActivity(intent)
            }
            else {
                val intent = Intent(this, AnimeDetailsActivity::class.java)
                intent.putExtra("animeId", id)
                intent.putExtra("defaultTransition", true)
                startActivity(intent)
            }
        }
    }

    private fun translateSynopsis(translator: Translator) {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                try {
                    translator.translate(synopsisView.text as String)
                        .addOnSuccessListener { translatedText ->
                            loadingTranslate.hide()
                            translateButton.text = resources.getString(R.string.translate_original)
                            synopsisView.text = translatedText
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT)
                                .show()
                        }
                } catch (e: IllegalStateException) {
                    Log.d("MoeLog", e.message?:"")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("title", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (this::mangaDetails.isInitialized) {
            loadingView.visibility = View.GONE
        }
    }

    override fun finish() {
        val returnIntent = Intent()
        returnIntent.putExtra("entryUpdated", entryUpdated)
        setResult(Activity.RESULT_OK, returnIntent)
        super.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)

        val viewOnMal = menu?.findItem(R.id.view_on_mal)
        viewOnMal?.setOnMenuItemClickListener { _ ->
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
            val chromeIntent = builder.build()
            chromeIntent.launchUrl(this,
                Uri.parse("https://myanimelist.net/manga/$mangaId"))
            return@setOnMenuItemClickListener true
        }

        val share = menu?.findItem(R.id.share)
        share?.setOnMenuItemClickListener { _ ->
            ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setChooserTitle(mangaTitleView.text)
                .setText("https://myanimelist.net/manga/$mangaId")
                .startChooser()
            return@setOnMenuItemClickListener true
        }
        return super.onCreateOptionsMenu(menu)
    }
}