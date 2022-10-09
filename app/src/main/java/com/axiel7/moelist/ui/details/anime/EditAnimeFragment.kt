package com.axiel7.moelist.ui.details.anime

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.MaterialSpinnerAdapter
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.databinding.BottomSheetEditAnimeBinding
import com.axiel7.moelist.ui.base.BaseBottomSheetDialogFragment
import com.axiel7.moelist.ui.list.AnimeListViewModel
import com.axiel7.moelist.utils.InsetsHelper
import com.axiel7.moelist.utils.StringExtensions.formatListStatus
import com.axiel7.moelist.utils.StringExtensions.formatListStatusInverted
import com.axiel7.moelist.utils.StringExtensions.formatScore
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class EditAnimeFragment(
    private val myListStatus: MyAnimeListStatus?,
    private val animeId: Int,
    private val numEpisodes: Int
) : BaseBottomSheetDialogFragment<BottomSheetEditAnimeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetEditAnimeBinding
        get() = BottomSheetEditAnimeBinding::inflate
    private val viewModel: AnimeListViewModel by viewModels()
    private val statusItems: Array<String> by lazy {
        arrayOf(getString(R.string.watching),
            getString(R.string.completed),
            getString(R.string.on_hold),
            getString(R.string.dropped),
            getString(R.string.ptw))
    }
    private val dialogDelete: MaterialAlertDialogBuilder by lazy {
        MaterialAlertDialogBuilder(safeContext)
            .setTitle(resources.getString(R.string.delete))
            .setMessage(resources.getString(R.string.delete_confirmation))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                viewModel.deleteEntry(animeId)
            }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) binding.loading.hide()
    }

    override fun setup() {
        launchLifecycleStarted {
            viewModel.updateResponse.collectLatest {
                if (it.first != null) {
                    binding.loading.hide()
                    if (!it.first!!.error.isNullOrEmpty() || !it.first!!.message.isNullOrEmpty()) {
                        showToast("${it.first!!.error}: ${it.first!!.message}")
                    }
                    else dismiss()
                }
                else if (it.second == "Error") {
                    binding.loading.hide()
                    showToast(getString(R.string.error_updating_list))
                }
            }
        }

        //Set peek height to hide delete button
        InsetsHelper.getViewBottomHeight(
            view as ViewGroup,
            R.id.divider,
            (dialog as BottomSheetDialog).behavior
        )

        binding.applyButton.setOnClickListener {

            val statusCurrent = binding.statusField.text.toString().formatListStatusInverted(safeContext)
            val status = if (statusCurrent != myListStatus?.status) statusCurrent else null

            val scoreCurrent = binding.scoreSlider.value.toInt()
            val score = if (scoreCurrent != myListStatus?.score) scoreCurrent else null

            val episodesCurrent = binding.episodesField.text.toString().toIntOrNull()
            val episodes = when {
                episodesCurrent != myListStatus?.numEpisodesWatched -> episodesCurrent
                status == "completed" -> numEpisodes
                else -> null
            }

            binding.loading.show()
            viewModel.updateList(animeId, status, score, episodes)
        }

        binding.cancelButton.setOnClickListener { dismiss() }

        val adapter = MaterialSpinnerAdapter(safeContext, statusItems)
        binding.statusField.setAdapter(adapter)
        binding.statusField.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (statusItems[position] == getString(R.string.completed)) {
                binding.episodesField.setText(numEpisodes.toString())
            } else {
                binding.episodesField.setText(myListStatus?.numEpisodesWatched.toString())
            }
        }

        binding.scoreSlider.addOnChangeListener { _, value, _ ->
            binding.scoreText.text =
                "${getString(R.string.score_value)} ${value.toInt().formatScore(safeContext)}"
        }
        binding.scoreText.text =
            "${getString(R.string.score_value)} ${binding.scoreSlider.value.toInt().formatScore(safeContext)}"

        binding.deleteButton.setOnClickListener { dialogDelete.show() }

        binding.episodesFieldLayout.suffixText = "/$numEpisodes"
        binding.minusButton.setOnClickListener {
            val inputEpisodes = binding.episodesField.text.toString().toIntOrNull() ?: 0
            if (inputEpisodes > 0) {
                binding.episodesField.setText((inputEpisodes - 1).toString())
            }
        }
        binding.plusButton.setOnClickListener {
            val inputEpisodes = binding.episodesField.text.toString().toIntOrNull() ?: 0
            if (inputEpisodes < numEpisodes || numEpisodes == 0) {
                binding.episodesField.setText((inputEpisodes + 1).toString())
            }
        }

        if (myListStatus != null) {
            syncListStatus()
        } else {
            binding.statusField.setText(statusItems.last(), false)
        }

        // Episodes input validation
        binding.episodesFieldLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputEpisodes = text.toString().toIntOrNull()
            binding.episodesFieldLayout.error = when {
                text.isNullOrEmpty() -> getString(R.string.invalid_number)
                text.isBlank() -> getString(R.string.invalid_number)
                inputEpisodes == null -> getString(R.string.invalid_number)
                (numEpisodes != 0 && inputEpisodes > numEpisodes) -> getString(R.string.invalid_number)
                else -> null
            }
        }
    }

    private fun syncListStatus() {
        binding.episodesField.setText(myListStatus?.numEpisodesWatched.toString())
        binding.statusField.setText(myListStatus?.status.formatListStatus(safeContext), false)
        binding.scoreSlider.value = myListStatus?.score?.toFloat() ?: 0f
    }

}