package com.axiel7.moelist.ui.details

import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.RelatedsAdapter
import com.axiel7.moelist.adapter.ThemesAdapter
import com.axiel7.moelist.databinding.ActivityAnimeDetailsBinding
import com.axiel7.moelist.model.AnimeDetails
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.model.Related
import com.axiel7.moelist.model.Theme
import com.axiel7.moelist.ui.LoginActivity
import com.axiel7.moelist.ui.base.BaseActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
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

class AnimeDetailsActivity : BaseActivity<ActivityAnimeDetailsBinding>(), EditAnimeFragment.OnDataPass {

    override val bindingInflater: (LayoutInflater) -> ActivityAnimeDetailsBinding
        get() = ActivityAnimeDetailsBinding::inflate
    private lateinit var animeDetails: AnimeDetails
    private lateinit var bottomSheetDialog: BottomSheetDialogFragment
    private lateinit var relatedsAdapter: RelatedsAdapter
    private val relateds: MutableList<Related> = mutableListOf()
    private var entryUpdated: Boolean = false
    private var deleted: Boolean = false
    private var animeId = 1
    private var position = 0

    override fun setDecorFitsSystemWindows(value: Boolean) {
        super.setDecorFitsSystemWindows(true)
    }

    override fun setup() {
        setSupportActionBar(binding.detailsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.detailsToolbar.setNavigationOnClickListener { onBackPressed() }

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
        position = intent?.extras?.getInt("position", 0) ?: 0

        initViews()
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
                        Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<AnimeDetails>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }
    private fun initUpdateCall() {
        val updateListCall = malApiService
            .updateAnimeList(Urls.apiBaseUrl + "anime/$animeId/my_list_status", "plan_to_watch", null, null)
        patchCall(updateListCall)
    }
    private fun patchCall(call: Call<MyListStatus>) {
        call.enqueue(object :Callback<MyListStatus> {
            override fun onResponse(call: Call<MyListStatus>, response: Response<MyListStatus>) {
                if (response.isSuccessful) {
                    val myListStatus = response.body()!!
                    animeDetails.my_list_status = myListStatus
                    entryUpdated = true
                    val toastText = getString(R.string.added_ptw)
                    Snackbar.make(binding.root, toastText, Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(binding.root, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }
    private fun initViews() {
        binding.mainTitle.setOnLongClickListener {
            copyToClipboard(binding.mainTitle.text.toString())
            true
        }

        binding.loadingTranslate.hide()
        if (Locale.getDefault().language == "en") {
            binding.translateButton.visibility = View.GONE
        }
        else {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(Locale.getDefault().language)!!)
                .build()
            val translator = getClient(options)
            lifecycle.addObserver(translator)
            binding.translateButton.setOnClickListener {
                if (binding.synopsis.text == animeDetails.synopsis) {
                    binding.loadingTranslate.show()
                    translateSynopsis(translator)
                }
                else {
                    binding.translateButton.text = resources.getString(R.string.translate)
                    binding.synopsis.text = animeDetails.synopsis
                }
            }
        }

        val synopsisIcon = findViewById<ImageView>(R.id.synopsis_icon)
        synopsisIcon.setOnClickListener {
            if (binding.synopsis.maxLines==5) {
                binding.synopsis.maxLines = Int.MAX_VALUE
                synopsisIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_arrow_up_24))
            }
            else {
                binding.synopsis.maxLines = 5
                synopsisIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_arrow_down_24))
            }
        }

        TooltipCompat.setTooltipText(binding.rankText, getString(R.string.top_ranked))
        TooltipCompat.setTooltipText(binding.membersText, getString(R.string.members))
        TooltipCompat.setTooltipText(binding.numScoresText, getString(R.string.users_scores))
        TooltipCompat.setTooltipText(binding.popularityText, getString(R.string.popularity))

        relatedsAdapter = RelatedsAdapter(
            relateds,
            applicationContext,
            onClickListener = { _, related -> openDetails(related.node.id, related.node.media_type)} )
        binding.relatedsRecycler.adapter = relatedsAdapter
    }
    private fun setDataToViews() {
        bottomSheetDialog =
            EditAnimeFragment(animeDetails.my_list_status, animeId, animeDetails.num_episodes ?: 0, 0)
        val unknown = getString(R.string.unknown)
        //quit loading bar
        binding.loadingLayout.visibility = View.GONE

        changeFabAction()

        //poster and main title
        binding.animePoster.load(animeDetails.main_picture?.medium) {
            crossfade(true)
            crossfade(300)
            error(R.drawable.ic_launcher_foreground)
        }
        binding.animePoster.setOnClickListener { openFullPoster() }
        binding.mainTitle.text = animeDetails.title

        //media type
        val mediaTypeText = animeDetails.media_type?.let { StringFormat.formatMediaType(it, this) }
        binding.mediaTypeText.text = mediaTypeText

        //total episodes
        val numEpisodes = animeDetails.num_episodes
        var episodesText = "$numEpisodes ${getString(R.string.episodes)}"
        if (numEpisodes==0) {
            episodesText = "?? ${getString(R.string.episodes)}"
        }
        binding.episodesText.text = episodesText

        //media status
        val statusText = animeDetails.status?.let { StringFormat.formatStatus(it, this) }
        binding.statusText.text = statusText

        //score and synopsis
        binding.scoreText.text = animeDetails.mean.toString()
        binding.synopsis.text = animeDetails.synopsis

        //genres chips
        val genres = animeDetails.genres
        if (genres != null && binding.chipGroupGenres.childCount==0) {
            for (genre in genres) {
                val chip = Chip(binding.chipGroupGenres.context)
                chip.text = StringFormat.formatGenre(genre.name, applicationContext)
                binding.chipGroupGenres.addView(chip)
            }
        }

        //stats
        val topRank = animeDetails.rank
        val rankText = "#$topRank"
        binding.rankText.text = if (topRank==null) { "N/A" } else { rankText }

        val membersRank = animeDetails.num_scoring_users
        binding.numScoresText.text = NumberFormat.getInstance().format(membersRank)

        binding.membersText.text = NumberFormat.getInstance().format(animeDetails.num_list_users)

        val popularity = animeDetails.popularity
        val popularityText = "#$popularity"
        binding.popularityText.text = popularityText

        //more info
        val synonyms = animeDetails.alternative_titles?.synonyms
        val synonymsText = synonyms?.joinToString(separator = ",\n")
        binding.synonymsText.text = if (!synonymsText.isNullOrEmpty()) { synonymsText }
        else { "─" }

        binding.jpTitle.text = animeDetails.alternative_titles?.ja
        binding.jpTitle.text = if (!animeDetails.alternative_titles?.ja.isNullOrEmpty()) {
            animeDetails.alternative_titles?.ja }
        else { "─" }

        binding.startDateText.text = if (!animeDetails.start_date.isNullOrEmpty()) { animeDetails.start_date }
        else { unknown }
        binding.endDateText.text = if (!animeDetails.end_date.isNullOrEmpty()) { animeDetails.end_date }
        else { unknown }

        val year = animeDetails.start_season?.year
        var season = animeDetails.start_season?.season
        season = season?.let { StringFormat.formatSeason(it, this) }
        val seasonText = "$season $year"
        binding.seasonText.text = if (animeDetails.start_season!=null) { seasonText }
        else { unknown }

        val weekDay = StringFormat.formatWeekday(animeDetails.broadcast?.day_of_the_week, this)
        val startTime = animeDetails.broadcast?.start_time
        binding.broadcastText.text = if (animeDetails.broadcast!=null) { "$weekDay $startTime (JST)" }
        else { unknown }

        val duration = animeDetails.average_episode_duration?.div(60)
        val min = getString(R.string.minutes_abbreviation)
        val durationText = "$duration $min."
        binding.durationText.text = if (duration==0) { unknown } else { durationText }

        var sourceText =  animeDetails.source
        sourceText = sourceText?.let { StringFormat.formatSource(it, this) }
        binding.sourceText.text = sourceText

        val studios = animeDetails.studios
        val studiosNames = mutableListOf<String>()
        if (studios != null) {
            for (studio in studios) {
                studiosNames.add(studio.name)
            }
        }
        val studiosText = studiosNames.joinToString(separator = ",\n")
        binding.studiosText.text = if (studiosText.isNotEmpty()) { studiosText }
        else { unknown }

        val openings = animeDetails.opening_themes ?: mutableListOf()
        val openingRecycler = findViewById<RecyclerView>(R.id.opening_recycler)
        openingRecycler.adapter = ThemesAdapter(openings as MutableList<Theme>, this)

        val endings = animeDetails.ending_themes ?: mutableListOf()
        val endingRecycler = findViewById<RecyclerView>(R.id.ending_recycler)
        endingRecycler.adapter = ThemesAdapter(endings as MutableList<Theme>, this)

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
    }
    private fun changeFabAction() {
        //change fab behavior if not added
        if (animeDetails.my_list_status==null) {
            binding.editFab.text = getString(R.string.add)
            binding.editFab.setIconResource(R.drawable.ic_round_add_24)
            binding.editFab.setOnClickListener {
                initUpdateCall()
                binding.editFab.text = getString(R.string.edit)
                binding.editFab.setIconResource(R.drawable.ic_round_edit_24)
                binding.editFab.setOnClickListener {
                    bottomSheetDialog.show(supportFragmentManager, "Edit")
                }
            }
        } else {
            binding.editFab.text = getString(R.string.edit)
            binding.editFab.setIconResource(R.drawable.ic_round_edit_24)
            binding.editFab.setOnClickListener {
                bottomSheetDialog.show(supportFragmentManager, "Edit")
            }
        }
    }

    private fun openFullPoster() {
        val intent = Intent(this, FullPosterActivity::class.java)
        val options = ActivityOptions.makeSceneTransitionAnimation(
            this,
            binding.animePoster,
            binding.animePoster.transitionName
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
                    translator.translate(binding.synopsis.text as String)
                        .addOnSuccessListener { translatedText ->
                            binding.loadingTranslate.hide()
                            binding.translateButton.text = resources.getString(R.string.translate_original)
                            binding.synopsis.text = translatedText
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
            binding.loadingLayout.visibility = View.GONE
        }
    }

    override fun finish() {
        val returnIntent = Intent()
        returnIntent.putExtra("entryUpdated", entryUpdated)
        returnIntent.putExtra("deleted", deleted)
        returnIntent.putExtra("position", position)
        setResult(Activity.RESULT_OK, returnIntent)
        super.finish()
    }

    override fun onAnimeEntryUpdated(updated: Boolean, deleted: Boolean, position: Int) {
        entryUpdated = updated
        this.deleted = deleted
        changeFabAction()
        animeDb?.animeDetailsDao()?.insertAnimeDetails(animeDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)

        val viewOnMal = menu?.findItem(R.id.view_on_mal)
        viewOnMal?.setOnMenuItemClickListener { _ ->
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://myanimelist.net/anime/$animeId"))
            startActivity(Intent.createChooser(intent, viewOnMal.title))
            return@setOnMenuItemClickListener true
        }

        val share = menu?.findItem(R.id.share)
        share?.setOnMenuItemClickListener { _ ->
            ShareCompat.IntentBuilder(this@AnimeDetailsActivity)
                .setType("text/plain")
                .setChooserTitle(binding.mainTitle.text)
                .setText("https://myanimelist.net/anime/$animeId")
                .startChooser()
            return@setOnMenuItemClickListener true
        }
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        const val fields = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
                "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_episodes,start_season," +
                "broadcast,source,average_episode_duration,studios,opening_themes,ending_themes,related_anime{media_type},related_manga{media_type}"
    }
}