package com.axiel7.moelist.ui.details

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
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
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.RelatedsAdapter
import com.axiel7.moelist.model.AnimeDetails
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.model.Related
import com.axiel7.moelist.ui.BaseActivity
import com.axiel7.moelist.utils.*
import com.axiel7.moelist.utils.InsetsHelper.addSystemWindowInsetToMargin
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
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation.getClient
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*
import kotlin.random.Random

class AnimeDetailsActivity : BaseActivity() {

    //private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var fields: String
    private lateinit var animeDetails: AnimeDetails
    private lateinit var loadingView: FrameLayout
    private lateinit var editFab: ExtendedFloatingActionButton
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dialogView: View
    private lateinit var animePosterView: ShapeableImageView
    private lateinit var animeTitleView: TextView
    private lateinit var mediaTypeView: TextView
    private lateinit var totalEpisodesView: TextView
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
    private lateinit var seasonView: TextView
    private lateinit var broadcastView: TextView
    private lateinit var durationView: TextView
    private lateinit var sourceView: TextView
    private lateinit var studiosView: TextView
    private lateinit var relatedRecycler: RecyclerView
    private lateinit var relatedsAdapter: RelatedsAdapter
    private lateinit var episodesLayout: TextInputLayout
    private lateinit var episodesField: TextInputEditText
    private lateinit var statusLayout: TextInputLayout
    private lateinit var statusField: AutoCompleteTextView
    private lateinit var scoreSlider: Slider
    private lateinit var snackBarView: View
    private val relateds: MutableList<Related> = mutableListOf()
    private var entryUpdated: Boolean = false
    private var animeId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(R.id.anime_poster)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(R.id.anime_poster)
            duration = 250L
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_details)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBackgroundAlpha)

        val toolbar = findViewById<Toolbar>(R.id.details_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        //SharedPrefsHelpers.init(this)
        //sharedPref = SharedPrefsHelpers.instance!!

        val data = intent?.dataString
        animeId = if (data?.startsWith("https://myanimelist.net/anime") == true) {
            data.split("/")[4].toIntOrNull() ?: 1
        } else {
            intent.getIntExtra("animeId", 1)
        }
        if (animeId==-1) {
            animeId = Random(System.nanoTime()).nextInt(0, 5000)
        }
        fields = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
                "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_episodes,start_season," +
                "broadcast,source,average_episode_duration,studios,related_anime{media_type},related_manga{media_type}"

        snackBarView = findViewById(R.id.details_layout)
        initViews()
        setupBottomSheet()
        if (animeDb?.animeDetailsDao()?.getAnimeDetailsById(animeId)!=null) {
            animeDetails = animeDb?.animeDetailsDao()?.getAnimeDetailsById(animeId)!!
            setDataToViews()
        }

        initCalls()
    }
    private fun initCalls() {
        val detailsCall = malApiService.getAnimeDetails(Urls.apiBaseUrl + "anime/$animeId", fields)
        initDetailsCall(detailsCall)
    }
    private fun initDetailsCall(call: Call<AnimeDetails>?) {
        call?.enqueue(object : Callback<AnimeDetails> {
            override fun onResponse(call: Call<AnimeDetails>, response: Response<AnimeDetails>) {
                //Log.d("MoeLog", call.request().toString())

                when {
                    response.isSuccessful -> {
                        animeDetails = response.body()!!
                        setDataToViews()
                        animeDb?.animeDetailsDao()?.insertAnimeDetails(animeDetails)
                    }
                    response.code()==404 -> {
                        animeId = Random(System.nanoTime()).nextInt(0, 5000)
                        call.cancel()
                        val detailsCall = malApiService.getAnimeDetails(Urls.apiBaseUrl + "anime/$animeId", fields)
                        initDetailsCall(detailsCall)
                    }
                    response.code()==401 -> {
                        Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<AnimeDetails>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
            Snackbar.make(snackBarView, getString(R.string.no_changes), Snackbar.LENGTH_SHORT).show()
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
                    var toastText = getString(R.string.updated)
                    if (newEntry) {
                        toastText = getString(R.string.added_ptw)
                    }
                    Snackbar.make(snackBarView, toastText, Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(snackBarView, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
        animePosterView = findViewById(R.id.anime_poster)
        animeTitleView = findViewById(R.id.main_title)
        mediaTypeView = findViewById(R.id.media_type_text)
        totalEpisodesView = findViewById(R.id.episodes_text)
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
            val translator = getClient(options)
            lifecycle.addObserver(translator)
            translateButton.setOnClickListener {
                if (synopsisView.text == animeDetails.synopsis) {
                    loadingTranslate.show()
                    translateSynopsis(translator)
                }
                else {
                    translateButton.text = resources.getString(R.string.translate)
                    synopsisView.text = animeDetails.synopsis
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
        seasonView = findViewById(R.id.season_text)
        broadcastView = findViewById(R.id.broadcast_text)
        durationView = findViewById(R.id.duration_text)
        sourceView = findViewById(R.id.source_text)
        studiosView = findViewById(R.id.studios_text)

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
        editFab.addSystemWindowInsetToMargin(bottom = true)
        dialogView = layoutInflater.inflate(R.layout.bottom_sheet_edit_anime, null)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setOnDismissListener {
            if (animeDetails.my_list_status!=null) {
                syncListStatus(animeDetails.my_list_status!!)
            }
        }

        val applyButton = bottomSheetDialog.findViewById<Button>(R.id.apply_button)
        applyButton?.setOnClickListener {

            var status :String? = null
            val statusCurrent = StringFormat.formatListStatusInverted(statusField.text.toString(), this)
            val statusOrigin = animeDetails.my_list_status?.status

            var score :Int? = null
            val scoreCurrent = scoreSlider.value.toInt()
            val scoreOrigin = animeDetails.my_list_status?.score

            var episodes: Int? = null
            val episodesCurrent = episodesField.text.toString().toIntOrNull()
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
            if (status=="completed") {
                episodes = animeDetails.num_episodes
            }
            initUpdateCall(status, score, episodes, false)
            bottomSheetDialog.hide()
        }
        val cancelButton = bottomSheetDialog.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            syncListStatus(animeDetails.my_list_status!!)
            bottomSheetDialog.hide()
        }

        statusLayout = bottomSheetDialog.findViewById(R.id.status_layout)!!
        statusField = bottomSheetDialog.findViewById(R.id.status_field)!!
        val statusItems = listOf(getString(R.string.watching), getString(R.string.completed),
            getString(R.string.on_hold), getString(R.string.dropped), getString(R.string.ptw))
        val adapter = ArrayAdapter(this, R.layout.list_status_item, statusItems)
        (statusLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        episodesLayout = bottomSheetDialog.findViewById(R.id.episodes_field_layout)!!
        episodesField = bottomSheetDialog.findViewById(R.id.episodes_field)!!

        val scoreText = bottomSheetDialog.findViewById<TextView>(R.id.score_text)
        scoreSlider = bottomSheetDialog.findViewById(R.id.score_slider)!!
        scoreSlider.addOnChangeListener { _, value, _ ->
            val scoreTextValue = "${getString(R.string.score_value)} " + value.toInt().let { StringFormat.formatScore(it, this) }
            scoreText?.text = scoreTextValue
        }
        val scoreTextValue = "${getString(R.string.score_value)} " + scoreSlider.value.toInt().let { StringFormat.formatScore(it, this) }
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
        animePosterView.load(animeDetails.main_picture?.medium) {
            crossfade(true)
            crossfade(300)
            error(R.drawable.ic_launcher_foreground)
        }
        animePosterView.setOnClickListener { openFullPoster() }
        animeTitleView.text = animeDetails.title

        //media type
        val mediaTypeText = animeDetails.media_type?.let { StringFormat.formatMediaType(it, this) }
        mediaTypeView.text = mediaTypeText

        //total episodes
        val numEpisodes = animeDetails.num_episodes
        var episodesText = "$numEpisodes ${getString(R.string.episodes)}"
        if (numEpisodes==0) {
            episodesText = "?? ${getString(R.string.episodes)}"
        }
        totalEpisodesView.text = episodesText

        //media status
        val statusText = animeDetails.status?.let { StringFormat.formatStatus(it, this) }
        statusView.text = statusText

        //score and synopsis
        scoreView.text = animeDetails.mean.toString()
        synopsisView.text = animeDetails.synopsis

        //genres chips
        val genres = animeDetails.genres
        if (genres != null && genresView.childCount==0) {
            for (genre in genres) {
                val chip = Chip(genresView.context)
                chip.text = StringFormat.formatGenre(genre.name, applicationContext)
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
        else { unknown }
        endDateView.text = if (!animeDetails.end_date.isNullOrEmpty()) { animeDetails.end_date }
        else { unknown }

        val year = animeDetails.start_season?.year
        var season = animeDetails.start_season?.season
        season = season?.let { StringFormat.formatSeason(it, this) }
        val seasonText = "$season $year"
        seasonView.text = if (animeDetails.start_season!=null) { seasonText }
        else { unknown }

        val weekDay = StringFormat.formatWeekday(animeDetails.broadcast?.day_of_the_week, this)
        val startTime = animeDetails.broadcast?.start_time
        broadcastView.text = if (animeDetails.broadcast!=null) { "$weekDay $startTime (JST)" }
        else { unknown }

        val duration = animeDetails.average_episode_duration?.div(60)
        val durationText = "$duration min."
        durationView.text = if (duration==0) { unknown } else { durationText }

        var sourceText =  animeDetails.source
        sourceText = sourceText?.let { StringFormat.formatSource(it, this) }
        sourceView.text = sourceText

        val studios = animeDetails.studios
        val studiosNames = mutableListOf<String>()
        if (studios != null) {
            for (studio in studios) {
                studiosNames.add(studio.name)
            }
        }
        val studiosText = studiosNames.joinToString(separator = ",\n")
        studiosView.text = if (studiosText.isNotEmpty()) { studiosText }
        else { unknown }

        //relateds
        val relatedAnimes = animeDetails.related_anime
        val relatedMangas = animeDetails.related_manga
        if (relatedAnimes != null && !relateds.containsAll(relatedAnimes)) {
            relateds.addAll(relatedAnimes)
        }
        if (relatedMangas != null && !relateds.containsAll(relatedMangas)) {
            relateds.addAll(relatedMangas)
        }
        relatedsAdapter.notifyDataSetChanged()

        //bottom sheet edit
        episodesLayout.suffixText = "/$numEpisodes"
        if (animeDetails.my_list_status!=null) {
            syncListStatus(animeDetails.my_list_status!!)
        }
        //episodes input logic
        episodesLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputEpisodes = text.toString().toIntOrNull()
            if (numEpisodes!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputEpisodes==null
                    || inputEpisodes > numEpisodes!!) {
                    episodesLayout.error = getString(R.string.invalid_number)
                } else { episodesLayout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    episodesLayout.error = getString(R.string.invalid_number)
                } else { episodesLayout.error = null }
            }
        }
    }
    private fun changeFabAction() {
        //change fab behavior if not added
        if (animeDetails.my_list_status==null) {
            editFab.text = getString(R.string.add)
            editFab.setIconResource(R.drawable.ic_round_add_24)
            editFab.setOnClickListener {
                initUpdateCall("plan_to_watch", null, null, true)
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
    private fun syncListStatus(myListStatus: MyListStatus) {
        val watchedEpisodes = myListStatus.num_episodes_watched
        episodesField.setText(watchedEpisodes.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus.status, this)
        statusField.setText(statusValue, false)

        scoreSlider.value = myListStatus.score.toFloat()
    }

    private fun openFullPoster() {
        val intent = Intent(this, FullPosterActivity::class.java)
        val options = ActivityOptions.makeSceneTransitionAnimation(
            this,
            animePosterView,
            animePosterView.transitionName
        )
        val largePicture = animeDetails.main_picture?.large
        val mediumPicture = animeDetails.main_picture?.medium
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
                translator.translate(synopsisView.text as String)
                    .addOnSuccessListener { translatedText ->
                        loadingTranslate.hide()
                        translateButton.text = resources.getString(R.string.translate_original)
                        synopsisView.text = translatedText
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (this::animeDetails.isInitialized) {
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
                Uri.parse("https://myanimelist.net/anime/$animeId"))
            return@setOnMenuItemClickListener true
        }

        val share = menu?.findItem(R.id.share)
        share?.setOnMenuItemClickListener { _ ->
            ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setChooserTitle(animeTitleView.text)
                .setText("https://myanimelist.net/anime/$animeId")
                .startChooser()
            return@setOnMenuItemClickListener true
        }
        return super.onCreateOptionsMenu(menu)
    }
}