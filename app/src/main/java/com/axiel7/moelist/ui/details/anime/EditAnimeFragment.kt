package com.axiel7.moelist.ui.details.anime

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.MaterialSpinnerAdapter
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.databinding.BottomSheetEditAnimeBinding
import com.axiel7.moelist.ui.base.BaseBottomSheetDialogFragment
import com.axiel7.moelist.ui.list.AnimeListViewModel
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.InsetsHelper
import com.axiel7.moelist.utils.StringExtensions.formatListStatus
import com.axiel7.moelist.utils.StringExtensions.formatListStatusInverted
import com.axiel7.moelist.utils.StringExtensions.formatScore
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
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

    private var selectedStartDate = DateUtils.getTimeInMillisFromDateString(date = myListStatus?.startDate)
    private val startDatePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.select_date)
        .setSelection(selectedStartDate)
        .build()
    private var selectedEndDate = DateUtils.getTimeInMillisFromDateString(date = myListStatus?.endDate)
    private val endDatePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.select_date)
        .setSelection(selectedEndDate)
        .build()

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
                    viewModel.consumeUpdateResponse()
                }
                else if (it.second == "Error") {
                    binding.loading.hide()
                    showToast(getString(R.string.error_updating_list))
                    viewModel.consumeUpdateResponse()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            //Set peek height to hide delete button
            InsetsHelper.getViewBottomHeight(
                view as ViewGroup,
                R.id.divider,
                (dialog as BottomSheetDialog).behavior,
                offset = bottomInset.toFloat()
            )

            insets
        }

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

            val startDateCurrent = DateUtils.unixtimeToStringDate(selectedStartDate)
            val startDate = if (startDateCurrent != myListStatus?.startDate) startDateCurrent else null

            val endDateCurrent = DateUtils.unixtimeToStringDate(selectedEndDate)
            val endDate = if (endDateCurrent != myListStatus?.endDate) endDateCurrent else null

            val rewatchesCurrent = binding.rewatchField.text.toString().toIntOrNull()
            val rewatches = if (rewatchesCurrent != myListStatus?.numTimesRewatched) rewatchesCurrent else null

            binding.loading.show()
            viewModel.updateList(animeId, status, score, episodes, startDate, endDate, rewatches)
        }

        binding.cancelButton.setOnClickListener { dismiss() }

        val adapter = MaterialSpinnerAdapter(safeContext, statusItems)
        binding.statusField.setAdapter(adapter)
        binding.statusField.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (statusItems[position] == getString(R.string.completed)) {
                binding.episodesField.setText(numEpisodes.toString())
                selectedEndDate?.let { selectedEndDate = MaterialDatePicker.todayInUtcMilliseconds() }
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
                if (myListStatus?.status == "plan_to_watch" && inputEpisodes == 0) {
                    selectedStartDate?.let { selectedStartDate = MaterialDatePicker.todayInUtcMilliseconds() }
                    binding.statusField.setText(statusItems.first())
                }
            }
        }

        binding.startDateField.setOnClickListener {
            startDatePicker.show(childFragmentManager, "start_date_picker")
        }

        startDatePicker.addOnPositiveButtonClickListener {
            selectedStartDate = it
            binding.startDateField.setText(startDatePicker.headerText)
        }

        binding.endDateField.setOnClickListener {
            endDatePicker.show(childFragmentManager, "end_date_picker")
        }

        endDatePicker.addOnPositiveButtonClickListener {
            selectedEndDate = it
            binding.endDateField.setText(endDatePicker.headerText)
        }

        binding.minusRewatchButton.setOnClickListener {
            val inputRereads = binding.rewatchField.text.toString().toIntOrNull() ?: 0
            if (inputRereads > 0) {
                binding.rewatchField.setText((inputRereads - 1).toString())
            }
        }

        binding.plusRewatchButton.setOnClickListener {
            val inputRereads = binding.rewatchField.text.toString().toIntOrNull() ?: 0
            binding.rewatchField.setText((inputRereads + 1).toString())
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
        DateUtils.getLocalDateFromDateString(myListStatus?.startDate)?.let {
            binding.startDateField.setText(DateUtils.formatLocalDateToString(it))
        }
        DateUtils.getLocalDateFromDateString(myListStatus?.endDate)?.let {
            binding.endDateField.setText(DateUtils.formatLocalDateToString(it))
        }
        binding.rewatchField.setText(myListStatus?.numTimesRewatched.toString())
    }

}