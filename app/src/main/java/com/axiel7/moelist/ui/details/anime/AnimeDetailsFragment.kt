package com.axiel7.moelist.ui.details.anime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.RelatedsAdapter
import com.axiel7.moelist.adapter.ThemesAdapter
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.isManga
import com.axiel7.moelist.databinding.FragmentAnimeDetailsBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Extensions.openCustomTab
import com.axiel7.moelist.utils.InsetsHelper.addSystemWindowInsetToMargin
import com.axiel7.moelist.utils.StringExtensions.formatGenre
import com.axiel7.moelist.utils.StringExtensions.formatMediaType
import com.axiel7.moelist.utils.StringExtensions.formatSeason
import com.axiel7.moelist.utils.StringExtensions.formatSource
import com.axiel7.moelist.utils.StringExtensions.formatStatus
import com.axiel7.moelist.utils.StringExtensions.formatWeekday
import com.axiel7.moelist.utils.UseCases.copyToClipBoard
import com.google.android.material.chip.Chip
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

class AnimeDetailsFragment : BaseFragment<FragmentAnimeDetailsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAnimeDetailsBinding
        get() = FragmentAnimeDetailsBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: AnimeDetailsViewModel by viewModels()
    private lateinit var adapterRelateds: RelatedsAdapter
    private var bottomSheetDialog: EditAnimeFragment? = null

    override fun onResume() {
        super.onResume()
        binding.editFab.addSystemWindowInsetToMargin(bottom = true)
    }

    override fun setup() {

        launchLifecycleStarted {
            mainViewModel.selectedId.collectLatest {
                it?.let {
                    binding.loading.show()
                    viewModel.getAnimeDetails(it)
                }
            }
        }

        launchLifecycleStarted {
            viewModel.animeDetails.collectLatest {
                it?.let { setAnimeData(it) }
            }
        }

        launchLifecycleStarted {
            viewModel.relateds.collectLatest {
                if (it.isNotEmpty()) {
                    binding.relateds.visibility = View.VISIBLE
                    binding.relatedsRecycler.visibility = View.VISIBLE
                    adapterRelateds.setData(it)
                } else {
                    binding.relateds.visibility = View.GONE
                    binding.relatedsRecycler.visibility = View.GONE
                }
            }
        }

        launchLifecycleStarted {
            viewModel.response.collectLatest {
                if (it.first == RESPONSE_ERROR) {
                    showSnackbar(it.second)
                }
            }
        }

        initUI()
    }

    private fun initUI() {
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        //Title
        binding.mainTitle.setOnLongClickListener {
            binding.mainTitle.text.toString().copyToClipBoard(safeContext)
            true
        }

        binding.loadingTranslate.hide()
        //Translate
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
                if (binding.synopsis.text == viewModel.animeDetails.value?.synopsis) {
                    binding.loadingTranslate.show()
                    translateSynopsis(translator)
                }
                else {
                    binding.translateButton.text = resources.getString(R.string.translate)
                    binding.synopsis.text = viewModel.animeDetails.value?.synopsis
                }
            }
        }

        //Synopsis
        binding.synopsisIcon.setOnClickListener {
            if (binding.synopsis.maxLines == 5) {
                binding.synopsis.maxLines = Int.MAX_VALUE
                binding.synopsisIcon.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24)
            }
            else {
                binding.synopsis.maxLines = 5
                binding.synopsisIcon.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24)
            }
        }

        //Tooltips
        TooltipCompat.setTooltipText(binding.rankText, getString(R.string.top_ranked))
        binding.rankText.setOnClickListener { it.performLongClick() }
        TooltipCompat.setTooltipText(binding.membersText, getString(R.string.members))
        binding.membersText.setOnClickListener { it.performLongClick() }
        TooltipCompat.setTooltipText(binding.numScoresText, getString(R.string.users_scores))
        binding.numScoresText.setOnClickListener { it.performLongClick() }
        TooltipCompat.setTooltipText(binding.popularityText, getString(R.string.popularity))
        binding.popularityText.setOnClickListener { it.performLongClick() }

        //Relateds
        adapterRelateds = RelatedsAdapter(
            safeContext,
            onClick = { _, item ->
                mainViewModel.selectId(item.node.id)
                if (item.isManga()) mainActivity?.navigate(
                    idAction = R.id.action_mangaDetailsFragment_self
                )
            }
        )
        binding.relatedsRecycler.adapter = adapterRelateds

        binding.editFab.setOnClickListener {
            bottomSheetDialog?.show(parentFragmentManager, "Edit")
        }
    }

    private fun setAnimeData(animeDetails: AnimeDetails) {
        binding.detailsScroll.smoothScrollTo(0, 0)
        binding.loading.hide()

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.open_in_browser -> {
                    safeContext.openCustomTab("https://myanimelist.net/anime/${animeDetails.id}")
                    true
                }
                else -> false
            }
        }

        bottomSheetDialog = EditAnimeFragment(
            myListStatus = animeDetails.myListStatus,
            animeId = animeDetails.id,
            numEpisodes = animeDetails.numEpisodes ?: 0
        )

        // Change FAB if entry not added
        if (animeDetails.myListStatus == null) {
            binding.editFab.text = getString(R.string.add)
            binding.editFab.setIconResource(R.drawable.ic_round_add_24)
        } else {
            binding.editFab.text = getString(R.string.edit)
            binding.editFab.setIconResource(R.drawable.ic_round_edit_24)
        }

        binding.animePoster.load(animeDetails.mainPicture?.medium)
        binding.animePoster.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_global_fullPosterFragment,
                bundle = Bundle().apply { putString("poster_url", animeDetails.mainPicture?.large) }
            )
        }
        binding.mainTitle.text = animeDetails.title

        binding.mediaTypeText.text = animeDetails.mediaType?.formatMediaType(safeContext)

        binding.episodesText.text = if (animeDetails.numEpisodes == 0) "?? ${getString(R.string.episodes)}"
        else "${animeDetails.numEpisodes} ${getString(R.string.episodes)}"

        binding.statusText.text = animeDetails.status?.formatStatus(safeContext)

        binding.scoreText.text = animeDetails.mean.toString()
        binding.synopsis.text = animeDetails.synopsis

        // Genres chips
        val genres = animeDetails.genres
        if (genres != null && binding.chipGroupGenres.childCount == 0) {
            for (genre in genres) {
                Chip(binding.chipGroupGenres.context).apply {
                    text = genre.name.formatGenre(safeContext)
                    binding.chipGroupGenres.addView(this)
                }
            }
        }

        // Stats
        binding.rankText.text = if (animeDetails.rank == null) "N/A" else "#${animeDetails.rank}"

        binding.numScoresText.text = NumberFormat.getInstance().format(animeDetails.numScoringUsers)

        binding.membersText.text = NumberFormat.getInstance().format(animeDetails.numListUsers)

        binding.popularityText.text = "#${animeDetails.popularity}"

        // More info
        val synonymsText = animeDetails.alternativeTitles?.synonyms?.joinToString(separator = ",\n")
        binding.synonymsText.text = if (!synonymsText.isNullOrEmpty()) synonymsText else "─"

        val jpTitle = animeDetails.alternativeTitles?.ja
        binding.jpTitle.text = if (!jpTitle.isNullOrEmpty()) jpTitle else "─"

        val unknown = getString(R.string.unknown)
        binding.startDateText.text = if (!animeDetails.startDate.isNullOrEmpty()) animeDetails.startDate
        else unknown
        binding.endDateText.text = if (!animeDetails.endDate.isNullOrEmpty()) animeDetails.endDate
        else unknown

        val year = animeDetails.startSeason?.year
        val season = animeDetails.startSeason?.season?.formatSeason(safeContext)
        val seasonText = "$season $year"
        binding.seasonText.text = if (animeDetails.startSeason != null) seasonText else unknown

        val weekDay = animeDetails.broadcast?.dayOfTheWeek?.formatWeekday(safeContext)
        val startTime = animeDetails.broadcast?.startTime
        binding.broadcastText.text = if (animeDetails.broadcast!=null) "$weekDay $startTime (JST)"
        else unknown

        val duration = animeDetails.averageEpisodeDuration?.div(60)
        val durationText = "$duration ${getString(R.string.minutes_abbreviation)}."
        binding.durationText.text = if (duration == 0) unknown else durationText

        binding.sourceText.text = animeDetails.source?.formatSource(safeContext)

        val studiosNames = mutableListOf<String>()
        animeDetails.studios?.forEach {
            studiosNames.add(it.name)
        }
        val studiosText = studiosNames.joinToString(separator = ",\n")
        binding.studiosText.text = if (studiosText.isNotEmpty()) studiosText else unknown

        if (animeDetails.openingThemes.isNullOrEmpty()) {
            binding.opening.visibility = View.GONE
            binding.openingRecycler.visibility = View.GONE
        } else {
            binding.opening.visibility = View.VISIBLE
            binding.openingRecycler.visibility = View.VISIBLE
            binding.openingRecycler.adapter = ThemesAdapter(safeContext).apply {
                setData(animeDetails.openingThemes)
            }
        }

        if (animeDetails.endingThemes.isNullOrEmpty()) {
            binding.ending.visibility = View.GONE
            binding.endingRecycler.visibility = View.GONE
        } else {
            binding.ending.visibility = View.VISIBLE
            binding.endingRecycler.visibility = View.VISIBLE
            binding.endingRecycler.adapter = ThemesAdapter(safeContext).apply {
                setData(animeDetails.endingThemes)
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
                            showSnackbar(exception.localizedMessage)
                        }
                } catch (e: IllegalStateException) {
                    Log.d("MoeLog", e.message?:"")
                }
            }
            .addOnFailureListener { exception ->
                showSnackbar(exception.localizedMessage)
            }
    }

}