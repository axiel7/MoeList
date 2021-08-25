package com.axiel7.moelist.ui.details.manga

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
import com.axiel7.moelist.data.model.isManga
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.databinding.FragmentMangaDetailsBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.Extensions.openLink
import com.axiel7.moelist.utils.StringExtensions.formatGenre
import com.axiel7.moelist.utils.StringExtensions.formatMediaType
import com.axiel7.moelist.utils.StringExtensions.formatStatus
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

class MangaDetailsFragment : BaseFragment<FragmentMangaDetailsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMangaDetailsBinding
        get() = FragmentMangaDetailsBinding::inflate
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: MangaDetailsViewModel by viewModels()
    private lateinit var adapterRelateds: RelatedsAdapter
    private var bottomSheetDialog: EditMangaFragment? = null

    override fun onResume() {
        super.onResume()
        binding.editFab.addSystemWindowInsetToMargin(bottom = true)
    }

    override fun setup() {

        launchLifecycleStarted {
            mainViewModel.selectedId.collectLatest {
                it?.let {
                    binding.loading.show()
                    viewModel.getMangaDetails(it)
                }
            }
        }

        launchLifecycleStarted {
            viewModel.mangaDetails.collectLatest {
                it?.let { setMangaData(it) }
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
                if (binding.synopsis.text == viewModel.mangaDetails.value?.synopsis) {
                    binding.loadingTranslate.show()
                    translateSynopsis(translator)
                }
                else {
                    binding.translateButton.text = resources.getString(R.string.translate)
                    binding.synopsis.text = viewModel.mangaDetails.value?.synopsis
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
                if (!item.isManga()) mainActivity?.navigate(
                    idAction = R.id.action_animeDetailsFragment_self
                )
            }
        )
        binding.relatedsRecycler.adapter = adapterRelateds

        binding.editFab.setOnClickListener {
            bottomSheetDialog?.show(parentFragmentManager, "Edit")
        }
    }

    private fun setMangaData(mangaDetails: MangaDetails) {
        binding.loading.hide()

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.open_in_browser -> {
                    safeContext.openLink("https://myanimelist.net/anime/${mangaDetails.id}")
                    true
                }
                else -> false
            }
        }

        bottomSheetDialog = EditMangaFragment(
            mangaDetails.myListStatus,
            mangaDetails.id,
            mangaDetails.numChapters ?: 0,
            mangaDetails.numVolumes ?: 0
        )

        // Change FAB if entry not added
        if (mangaDetails.myListStatus == null) {
            binding.editFab.text = getString(R.string.add)
            binding.editFab.setIconResource(R.drawable.ic_round_add_24)
        } else {
            binding.editFab.text = getString(R.string.edit)
            binding.editFab.setIconResource(R.drawable.ic_round_edit_24)
        }

        binding.mangaPoster.load(mangaDetails.mainPicture?.medium)
        binding.mangaPoster.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_global_fullPosterFragment,
                bundle = Bundle().apply { putString("poster_url", mangaDetails.mainPicture?.large) }
            )
        }
        binding.mainTitle.text = mangaDetails.title

        binding.mediaTypeText.text = mangaDetails.mediaType?.formatMediaType(safeContext)

        binding.chaptersText.text = if (mangaDetails.numChapters == 0) "?? ${getString(R.string.chapters)}"
        else "${mangaDetails.numChapters} ${getString(R.string.chapters)}"

        binding.volumesText.text = if (mangaDetails.numVolumes == 0) "?? ${getString(R.string.volumes)}"
        else "${mangaDetails.numVolumes} ${getString(R.string.volumes)}"

        binding.statusText.text = mangaDetails.status?.formatStatus(safeContext)

        binding.scoreText.text = mangaDetails.mean.toString()
        binding.synopsis.text = mangaDetails.synopsis

        // Genres chips
        val genres = mangaDetails.genres
        if (genres != null && binding.chipGroupGenres.childCount == 0) {
            for (genre in genres) {
                Chip(binding.chipGroupGenres.context).apply {
                    text = genre.name.formatGenre(safeContext)
                    binding.chipGroupGenres.addView(this)
                }
            }
        }

        // Stats
        binding.rankText.text = if (mangaDetails.rank == null) "N/A" else "#${mangaDetails.rank}"

        binding.numScoresText.text = NumberFormat.getInstance().format(mangaDetails.numScoringUsers)

        binding.membersText.text = NumberFormat.getInstance().format(mangaDetails.numListUsers)

        binding.popularityText.text = "#${mangaDetails.popularity}"

        // More info
        val synonymsText = mangaDetails.alternativeTitles?.synonyms?.joinToString(separator = ",\n")
        binding.synonymsText.text = if (!synonymsText.isNullOrEmpty()) synonymsText else "─"

        val jpTitle = mangaDetails.alternativeTitles?.ja
        binding.jpTitle.text = if (!jpTitle.isNullOrEmpty()) jpTitle else "─"

        val unknown = getString(R.string.unknown)
        binding.startDateText.text = if (!mangaDetails.startDate.isNullOrEmpty()) mangaDetails.startDate
        else unknown
        binding.endDateText.text = if (!mangaDetails.endDate.isNullOrEmpty()) mangaDetails.endDate
        else unknown

        // Authors
        val authorsRoles = mutableListOf<String>()
        val authorsNames = mutableListOf<String>()
        val authorsSurnames = mutableListOf<String>()
        val authorsText = mutableListOf<String>()
        mangaDetails.authors?.forEach {
            authorsRoles.add(it.role)
            authorsNames.add(it.node.firstName)
            authorsSurnames.add(it.node.lastName)
        }
        mangaDetails.authors?.indices?.forEach {
            authorsText.add("${authorsNames[it]} ${authorsSurnames[it]} (${authorsRoles[it]})")
        }

        val authorsTextJoin = authorsText.joinToString(separator = ",\n")
        binding.authorsText.text = if (authorsTextJoin.isNotEmpty()) authorsTextJoin else unknown

        // Serialization
        val serialNames = mutableListOf<String>()
        mangaDetails.serialization?.forEach {
            serialNames.add(it.node.name)
        }
        val serialText = serialNames.joinToString(separator = ",\n")
        binding.serialText.text = if (serialText.isNotEmpty()) serialText else unknown
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