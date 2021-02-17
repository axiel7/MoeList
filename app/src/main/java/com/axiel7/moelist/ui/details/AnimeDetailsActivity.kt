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
import androidx.appcompat.widget.TooltipCompat
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.RelatedsAdapter
import com.axiel7.moelist.adapter.ThemesAdapter
import com.axiel7.moelist.model.AnimeDetails
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.model.Related
import com.axiel7.moelist.model.Theme
import com.axiel7.moelist.ui.BaseActivity
import com.axiel7.moelist.ui.LoginActivity
import com.axiel7.moelist.utils.*
import com.axiel7.moelist.utils.InsetsHelper.getViewBottomHeight
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation.getClient
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.android.synthetic.main.activity_anime_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*
import kotlin.random.Random

class AnimeDetailsActivity : BaseActivity() {

    private lateinit var fields: String
    private lateinit var animeDetails: AnimeDetails
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dialogView: View
    private lateinit var relatedsAdapter: RelatedsAdapter
    private lateinit var episodesLayout: TextInputLayout
    private lateinit var episodesField: TextInputEditText
    private lateinit var statusLayout: TextInputLayout
    private lateinit var statusField: AutoCompleteTextView
    private lateinit var scoreSlider: Slider
    private val relateds: MutableList<Related> = mutableListOf()
    private var entryUpdated: Boolean = false
    private var animeId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_details)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setSupportActionBar(details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        details_toolbar.setNavigationOnClickListener { onBackPressed() }

        if (!MyApplication.isUserLogged) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                "broadcast,source,average_episode_duration,studios,opening_themes,ending_themes,related_anime{media_type},related_manga{media_type}"

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
                        Snackbar.make(details_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<AnimeDetails>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                Snackbar.make(details_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
            Snackbar.make(details_layout, getString(R.string.no_changes), Snackbar.LENGTH_SHORT).show()
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
                    Snackbar.make(details_layout, toastText, Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(details_layout, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Snackbar.make(details_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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
                    Snackbar.make(details_layout, getString(R.string.deleted), Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(details_layout, getString(R.string.error_delete_entry), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Snackbar.make(details_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }

        })
    }
    private fun initViews() {
        main_title.setOnLongClickListener {
            copyToClipboard(main_title.text.toString())
            true
        }

        loading_translate.hide()
        if (Locale.getDefault().language == "en") {
            translate_button.visibility = View.GONE
        }
        else {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(Locale.getDefault().language)!!)
                .build()
            val translator = getClient(options)
            lifecycle.addObserver(translator)
            translate_button.setOnClickListener {
                if (synopsis.text == animeDetails.synopsis) {
                    loading_translate.show()
                    translateSynopsis(translator)
                }
                else {
                    translate_button.text = resources.getString(R.string.translate)
                    synopsis.text = animeDetails.synopsis
                }
            }
        }

        val synopsisIcon = findViewById<ImageView>(R.id.synopsis_icon)
        synopsisIcon.setOnClickListener {
            if (synopsis.maxLines==5) {
                synopsis.maxLines = Int.MAX_VALUE
                synopsisIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_arrow_up_24))
            }
            else {
                synopsis.maxLines = 5
                synopsisIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_arrow_down_24))
            }
        }

        TooltipCompat.setTooltipText(rank_text, getString(R.string.top_ranked))
        TooltipCompat.setTooltipText(members_text, getString(R.string.members))
        TooltipCompat.setTooltipText(num_scores_text, getString(R.string.users_scores))
        TooltipCompat.setTooltipText(popularity_text, getString(R.string.popularity))

        relatedsAdapter = RelatedsAdapter(
            relateds,
            R.layout.list_item_anime_related,
            applicationContext,
            onClickListener = { _, related -> openDetails(related.node.id, related.node.media_type)} )
        relateds_recycler.adapter = relatedsAdapter
    }
    @SuppressLint("InflateParams")
    private fun setupBottomSheet() {
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
        loading_layout.visibility = View.GONE

        changeFabAction()

        //poster and main title
        anime_poster.load(animeDetails.main_picture?.medium) {
            crossfade(true)
            crossfade(300)
            error(R.drawable.ic_launcher_foreground)
        }
        anime_poster.setOnClickListener { openFullPoster() }
        main_title.text = animeDetails.title

        //media type
        val mediaTypeText = animeDetails.media_type?.let { StringFormat.formatMediaType(it, this) }
        media_type_text.text = mediaTypeText

        //total episodes
        val numEpisodes = animeDetails.num_episodes
        var episodesText = "$numEpisodes ${getString(R.string.episodes)}"
        if (numEpisodes==0) {
            episodesText = "?? ${getString(R.string.episodes)}"
        }
        episodes_text.text = episodesText

        //media status
        val statusText = animeDetails.status?.let { StringFormat.formatStatus(it, this) }
        status_text.text = statusText

        //score and synopsis
        score_text.text = animeDetails.mean.toString()
        synopsis.text = animeDetails.synopsis

        //genres chips
        val genres = animeDetails.genres
        if (genres != null && chip_group_genres.childCount==0) {
            for (genre in genres) {
                val chip = Chip(chip_group_genres.context)
                chip.text = StringFormat.formatGenre(genre.name, applicationContext)
                chip_group_genres.addView(chip)
            }
        }

        //stats
        val topRank = animeDetails.rank
        val rankText = "#$topRank"
        rank_text.text = if (topRank==null) { "N/A" } else { rankText }

        val membersRank = animeDetails.num_scoring_users
        num_scores_text.text = NumberFormat.getInstance().format(membersRank)

        members_text.text = NumberFormat.getInstance().format(animeDetails.num_list_users)

        val popularity = animeDetails.popularity
        val popularityText = "#$popularity"
        popularity_text.text = popularityText

        //more info
        val synonyms = animeDetails.alternative_titles?.synonyms
        val synonymsText = synonyms?.joinToString(separator = ",\n")
        synonyms_text.text = if (!synonymsText.isNullOrEmpty()) { synonymsText }
        else { "─" }

        jp_title.text = animeDetails.alternative_titles?.ja
        jp_title.text = if (!animeDetails.alternative_titles?.ja.isNullOrEmpty()) {
            animeDetails.alternative_titles?.ja }
        else { "─" }

        start_date_text.text = if (!animeDetails.start_date.isNullOrEmpty()) { animeDetails.start_date }
        else { unknown }
        end_date_text.text = if (!animeDetails.end_date.isNullOrEmpty()) { animeDetails.end_date }
        else { unknown }

        val year = animeDetails.start_season?.year
        var season = animeDetails.start_season?.season
        season = season?.let { StringFormat.formatSeason(it, this) }
        val seasonText = "$season $year"
        season_text.text = if (animeDetails.start_season!=null) { seasonText }
        else { unknown }

        val weekDay = StringFormat.formatWeekday(animeDetails.broadcast?.day_of_the_week, this)
        val startTime = animeDetails.broadcast?.start_time
        broadcast_text.text = if (animeDetails.broadcast!=null) { "$weekDay $startTime (JST)" }
        else { unknown }

        val duration = animeDetails.average_episode_duration?.div(60)
        val min = getString(R.string.minutes_abbreviation)
        val durationText = "$duration $min."
        duration_text.text = if (duration==0) { unknown } else { durationText }

        var sourceText =  animeDetails.source
        sourceText = sourceText?.let { StringFormat.formatSource(it, this) }
        source_text.text = sourceText

        val studios = animeDetails.studios
        val studiosNames = mutableListOf<String>()
        if (studios != null) {
            for (studio in studios) {
                studiosNames.add(studio.name)
            }
        }
        val studiosText = studiosNames.joinToString(separator = ",\n")
        studios_text.text = if (studiosText.isNotEmpty()) { studiosText }
        else { unknown }

        val openings = animeDetails.opening_themes ?: mutableListOf()
        val openingRecycler = findViewById<RecyclerView>(R.id.opening_recycler)
        openingRecycler.adapter = ThemesAdapter(openings as MutableList<Theme>,
            R.layout.list_item_theme, this)

        val endings = animeDetails.ending_themes ?: mutableListOf()
        val endingRecycler = findViewById<RecyclerView>(R.id.ending_recycler)
        endingRecycler.adapter = ThemesAdapter(endings as MutableList<Theme>,
            R.layout.list_item_theme, this)

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
            edit_fab.text = getString(R.string.add)
            edit_fab.setIconResource(R.drawable.ic_round_add_24)
            edit_fab.setOnClickListener {
                initUpdateCall("plan_to_watch", null, null, true)
                edit_fab.text = getString(R.string.edit)
                edit_fab.setIconResource(R.drawable.ic_round_edit_24)
                val bottomSheetBehavior = bottomSheetDialog.behavior
                edit_fab.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    getViewBottomHeight(dialogView as ViewGroup, R.id.divider, bottomSheetBehavior)
                    bottomSheetDialog.show()
                }
            }
        } else {
            edit_fab.text = getString(R.string.edit)
            edit_fab.setIconResource(R.drawable.ic_round_edit_24)
            val bottomSheetBehavior = bottomSheetDialog.behavior
            edit_fab.setOnClickListener {
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
            anime_poster,
            anime_poster.transitionName
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
                try {
                    translator.translate(synopsis.text as String)
                        .addOnSuccessListener { translatedText ->
                            loading_translate.hide()
                            translate_button.text = resources.getString(R.string.translate_original)
                            synopsis.text = translatedText
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
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
        if (this::animeDetails.isInitialized) {
            loading_layout.visibility = View.GONE
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
            builder.setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .build())
            val chromeIntent = builder.build()
            chromeIntent.launchUrl(this,
                Uri.parse("https://myanimelist.net/anime/$animeId"))
            return@setOnMenuItemClickListener true
        }

        val share = menu?.findItem(R.id.share)
        share?.setOnMenuItemClickListener { _ ->
            ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setChooserTitle(main_title.text)
                .setText("https://myanimelist.net/anime/$animeId")
                .startChooser()
            return@setOnMenuItemClickListener true
        }
        return super.onCreateOptionsMenu(menu)
    }
}