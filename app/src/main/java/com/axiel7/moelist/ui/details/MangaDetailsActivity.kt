package com.axiel7.moelist.ui.details

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
import android.widget.*
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import coil.load
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.MyApplication.Companion.animeDb
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.RelatedsAdapter
import com.axiel7.moelist.databinding.ActivityMangaDetailsBinding
import com.axiel7.moelist.model.MangaDetails
import com.axiel7.moelist.model.MyMangaListStatus
import com.axiel7.moelist.model.Related
import com.axiel7.moelist.ui.BaseActivity
import com.axiel7.moelist.ui.LoginActivity
import com.axiel7.moelist.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
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

class MangaDetailsActivity : BaseActivity(), EditMangaFragment.OnDataPass {

    private lateinit var mangaDetails: MangaDetails
    private lateinit var bottomSheetDialog: EditMangaFragment
    private lateinit var relatedsAdapter: RelatedsAdapter
    private val relateds: MutableList<Related> = mutableListOf()
    private var entryUpdated: Boolean = false
    private var mangaId = 1
    private var position = 0
    private lateinit var binding: ActivityMangaDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMangaDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)

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
        mangaId = if (data?.startsWith("https://myanimelist.net/manga") == true) {
            data.split("/")[4].toIntOrNull() ?: 1
        } else {
            intent.getIntExtra("mangaId", 1)
        }
        position = intent?.extras?.getInt("position", 0) ?: 0

        initViews()
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
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MangaDetails>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }
    private fun initUpdateCall() {
        val updateListCall = malApiService
            .updateMangaList(Urls.apiBaseUrl + "manga/$mangaId/my_list_status", "plan_to_read", null, null, null)
        patchCall(updateListCall)
    }
    private fun patchCall(call: Call<MyMangaListStatus>) {
        call.enqueue(object :Callback<MyMangaListStatus> {
            override fun onResponse(call: Call<MyMangaListStatus>, response: Response<MyMangaListStatus>) {
                if (response.isSuccessful) {
                    val myListStatus = response.body()!!
                    mangaDetails.my_list_status = myListStatus
                    entryUpdated = true
                    val toastText = getString(R.string.added_ptr)
                    Snackbar.make(binding.root, toastText, Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Snackbar.make(binding.root, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyMangaListStatus>, t: Throwable) {
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
            val translator = Translation.getClient(options)
            lifecycle.addObserver(translator)
            binding.translateButton.setOnClickListener {
                if (binding.synopsis.text == mangaDetails.synopsis) {
                    binding.loadingTranslate.show()
                    translateSynopsis(translator)
                }
                else {
                    binding.translateButton.text = resources.getString(R.string.translate)
                    binding.synopsis.text = mangaDetails.synopsis
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
            EditMangaFragment(mangaDetails.my_list_status, mangaId,
                mangaDetails.num_chapters ?: 0,
                mangaDetails.num_volumes ?: 0, 0)
        val unknown = getString(R.string.unknown)
        //quit loading bar
        binding.loadingLayout.visibility = View.GONE

        changeFabAction()

        //poster and main title
        binding.mangaPoster.load(mangaDetails.main_picture?.medium) {
            crossfade(true)
            crossfade(300)
            error(R.drawable.ic_launcher_foreground)
        }
        binding.mangaPoster.setOnClickListener { openFullPoster() }
        binding.mainTitle.text = mangaDetails.title

        //media type
        val mediaTypeText = mangaDetails.media_type?.let { StringFormat.formatMediaType(it, this) }
        binding.mediaTypeText.text = mediaTypeText

        //total episodes
        val numChapters = mangaDetails.num_chapters
        var episodesText = "$numChapters ${getString(R.string.chapters)}"
        if (numChapters==0) {
            episodesText = "?? ${getString(R.string.chapters)}"
        }
        binding.episodesText.text = episodesText
        // total volumes
        val numVolumes = mangaDetails.num_volumes
        var volumesText = "$numVolumes ${getString(R.string.volumes)}"
        if (numVolumes==0) {
            volumesText = "?? ${getString(R.string.volumes)}"
        }
        binding.volumesText.text = volumesText

        //media status
        val statusText = mangaDetails.status?.let { StringFormat.formatStatus(it, this) }
        binding.statusText.text = statusText

        //score and synopsis
        binding.scoreText.text = mangaDetails.mean.toString()
        binding.synopsis.text = mangaDetails.synopsis

        //genres chips
        val genres = mangaDetails.genres
        if (genres != null && binding.chipGroupGenres.childCount==0) {
            for (genre in genres) {
                val chip = Chip(binding.chipGroupGenres.context)
                chip.text = StringFormat.formatGenre(genre.name, applicationContext)
                binding.chipGroupGenres.addView(chip)
            }
        }

        //stats
        val topRank = mangaDetails.rank
        val rankText = "#$topRank"
        binding.rankText.text = if (topRank==null) { "N/A" } else { rankText }

        val membersRank = mangaDetails.num_scoring_users
        binding.numScoresText.text = NumberFormat.getInstance().format(membersRank)

        binding.membersText.text = NumberFormat.getInstance().format(mangaDetails.num_list_users)

        val popularity = mangaDetails.popularity
        val popularityText = "#$popularity"
        binding.popularityText.text = popularityText

        //more info
        val synonyms = mangaDetails.alternative_titles?.synonyms
        val synonymsText = synonyms?.joinToString(separator = ",\n")
        binding.synonymsText.text = if (!synonymsText.isNullOrEmpty()) { synonymsText }
        else { "─" }

        binding.jpTitle.text = if (!mangaDetails.alternative_titles?.ja.isNullOrEmpty()) {
            mangaDetails.alternative_titles?.ja }
        else { "─" }

        binding.startDateText.text = if (!mangaDetails.start_date.isNullOrEmpty()) { mangaDetails.start_date }
        else { unknown }
        binding.endDateText.text = if (!mangaDetails.end_date.isNullOrEmpty()) { mangaDetails.end_date }
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
        binding.authorsText.text = if (authorsTextFormatted.isNotEmpty()) { authorsTextFormatted }
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
        binding.serialText.text = if (serialText.isNotEmpty()) { serialText }
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
    }
    private fun changeFabAction() {
        //change fab behavior if not added
        if (mangaDetails.my_list_status==null) {
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
            binding.mangaPoster,
            binding.mangaPoster.transitionName
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
                    translator.translate(binding.synopsis.text as String)
                        .addOnSuccessListener { translatedText ->
                            binding.loadingTranslate.hide()
                            binding.translateButton.text = resources.getString(R.string.translate_original)
                            binding.synopsis.text = translatedText
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
            binding.loadingLayout.visibility = View.GONE
        }
    }

    override fun finish() {
        val returnIntent = Intent()
        returnIntent.putExtra("entryUpdated", entryUpdated)
        returnIntent.putExtra("position", position)
        setResult(Activity.RESULT_OK, returnIntent)
        super.finish()
    }

    override fun onMangaEntryUpdated(updated: Boolean, position: Int) {
        entryUpdated = updated
        changeFabAction()
        animeDb?.mangaDetailsDao()?.insertMangaDetails(mangaDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)

        val viewOnMal = menu?.findItem(R.id.view_on_mal)
        viewOnMal?.setOnMenuItemClickListener { _ ->
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://myanimelist.net/manga/$mangaId"))
            startActivity(Intent.createChooser(intent, viewOnMal.title))
            return@setOnMenuItemClickListener true
        }

        val share = menu?.findItem(R.id.share)
        share?.setOnMenuItemClickListener { _ ->
            ShareCompat.IntentBuilder(this@MangaDetailsActivity)
                .setType("text/plain")
                .setChooserTitle(binding.mainTitle.text)
                .setText("https://myanimelist.net/manga/$mangaId")
                .startChooser()
            return@setOnMenuItemClickListener true
        }
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        const val fields = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
            "num_list_users,num_scoring_users,media_type,status,genres,my_list_status,num_chapters,num_volumes," +
            "source,authors{first_name,last_name},serialization,related_anime{media_type},related_manga{media_type}"
    }
}