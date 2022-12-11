package com.axiel7.moelist.ui.details.manga

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.MaterialSpinnerAdapter
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.databinding.BottomSheetEditMangaBinding
import com.axiel7.moelist.ui.base.BaseBottomSheetDialogFragment
import com.axiel7.moelist.ui.list.MangaListViewModel
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.InsetsHelper
import com.axiel7.moelist.utils.StringExtensions.formatListStatus
import com.axiel7.moelist.utils.StringExtensions.formatListStatusInverted
import com.axiel7.moelist.utils.StringExtensions.formatScore
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class EditMangaFragment(
    private val myListStatus: MyMangaListStatus?,
    private val mangaId: Int,
    private val numChapters: Int,
    private val numVolumes: Int
) : BaseBottomSheetDialogFragment<BottomSheetEditMangaBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetEditMangaBinding
        get() = BottomSheetEditMangaBinding::inflate
    private val viewModel: MangaListViewModel by viewModels()
    private val statusItems: Array<String> by lazy {
        arrayOf(getString(R.string.reading),
            getString(R.string.completed),
            getString(R.string.on_hold),
            getString(R.string.dropped),
            getString(R.string.ptr)
        )
    }
    private val dialogDelete: MaterialAlertDialogBuilder by lazy {
        MaterialAlertDialogBuilder(safeContext)
            .setTitle(resources.getString(R.string.delete))
            .setMessage(resources.getString(R.string.delete_confirmation))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                viewModel.deleteEntry(mangaId)
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

            val chaptersCurrent = binding.chaptersField.text.toString().toIntOrNull()
            val chapters = when {
                chaptersCurrent != myListStatus?.numChaptersRead -> chaptersCurrent
                status == "completed" -> numChapters
                else -> null
            }

            val volumesCurrent = binding.volumesField.text.toString().toIntOrNull()
            val volumes = when {
                volumesCurrent != myListStatus?.numVolumesRead -> volumesCurrent
                status == "completed" -> numVolumes
                else -> null
            }

            val startDateCurrent = DateUtils.unixtimeToStringDate(selectedStartDate)
            val startDate = if (startDateCurrent != myListStatus?.startDate) startDateCurrent else null

            val endDateCurrent = DateUtils.unixtimeToStringDate(selectedEndDate)
            val endDate = if (endDateCurrent != myListStatus?.endDate) endDateCurrent else null

            val rereadsCurrent = binding.rereadField.text.toString().toIntOrNull()
            val rereads = if (rereadsCurrent != myListStatus?.numTimesReread) rereadsCurrent else null

            binding.loading.show()
            viewModel.updateList(mangaId, status, score, chapters, volumes, startDate, endDate, rereads)
        }

        binding.cancelButton.setOnClickListener { dismiss() }

        val adapter = MaterialSpinnerAdapter(safeContext, statusItems)
        binding.statusField.setAdapter(adapter)
        binding.statusField.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (statusItems[position] == getString(R.string.completed)) {
                binding.chaptersField.setText(numChapters.toString())
                binding.volumesField.setText(numVolumes.toString())
                selectedEndDate?.let { selectedEndDate = MaterialDatePicker.todayInUtcMilliseconds() }
            } else {
                binding.chaptersField.setText(myListStatus?.numChaptersRead.toString())
                binding.volumesField.setText(myListStatus?.numVolumesRead.toString())
            }
        }

        binding.scoreSlider.addOnChangeListener { _, value, _ ->
            binding.scoreText.text =
                "${getString(R.string.score_value)} ${value.toInt().formatScore(safeContext)}"
        }
        binding.scoreText.text =
            "${getString(R.string.score_value)} ${binding.scoreSlider.value.toInt().formatScore(safeContext)}"

        binding.deleteButton.setOnClickListener { dialogDelete.show() }

        binding.chaptersLayout.suffixText = "/$numChapters"
        binding.volumesLayout.suffixText = "/$numVolumes"

        binding.minusChButton.setOnClickListener {
            val inputChapters = binding.chaptersField.text.toString().toIntOrNull() ?: 0
            if (inputChapters > 0) {
                binding.chaptersField.setText((inputChapters - 1).toString())
            }
        }
        binding.plusChButton.setOnClickListener {
            val inputChapters = binding.chaptersField.text.toString().toIntOrNull() ?: 0
            if (inputChapters < numChapters || numChapters == 0) {
                binding.chaptersField.setText((inputChapters + 1).toString())
            }
            if (myListStatus?.status == "plan_to_read" && inputChapters == 0) {
                selectedStartDate?.let { selectedStartDate = MaterialDatePicker.todayInUtcMilliseconds() }
                binding.statusField.setText(statusItems.first())
            }
        }
        binding.minusVolButton.setOnClickListener {
            val inputVolumes = binding.volumesField.text.toString().toIntOrNull() ?: 0
            if (inputVolumes > 0) {
                binding.volumesField.setText((inputVolumes - 1).toString())
            }
        }
        binding.plusVolButton.setOnClickListener {
            val inputVolumes = binding.volumesField.text.toString().toIntOrNull() ?: 0
            if (inputVolumes < numVolumes || numVolumes == 0) {
                binding.volumesField.setText((inputVolumes + 1).toString())
            }
            if (myListStatus?.status == "plan_to_read" && inputVolumes == 0) {
                selectedStartDate?.let { selectedStartDate = MaterialDatePicker.todayInUtcMilliseconds() }
                binding.statusField.setText(statusItems.first())
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

        binding.minusRereadButton.setOnClickListener {
            val inputRereads = binding.rereadField.text.toString().toIntOrNull() ?: 0
            if (inputRereads > 0) {
                binding.rereadField.setText((inputRereads - 1).toString())
            }
        }

        binding.plusRereadButton.setOnClickListener {
            val inputRereads = binding.rereadField.text.toString().toIntOrNull() ?: 0
            binding.rereadField.setText((inputRereads + 1).toString())
        }

        if (myListStatus != null) {
            syncListStatus()
        } else {
            binding.statusField.setText(statusItems.last(), false)
        }

        // Chapters input validation
        binding.chaptersLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputChapters = text.toString().toIntOrNull()
            binding.chaptersLayout.error = when {
                text.isNullOrEmpty() -> getString(R.string.invalid_number)
                text.isBlank() -> getString(R.string.invalid_number)
                inputChapters == null -> getString(R.string.invalid_number)
                (numChapters != 0 && inputChapters > numChapters) -> getString(R.string.invalid_number)
                else -> null
            }
        }
        // Volumes input validation
        binding.volumesLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputVolumes = text.toString().toIntOrNull()
            binding.volumesLayout.error = when {
                text.isNullOrEmpty() -> getString(R.string.invalid_number)
                text.isBlank() -> getString(R.string.invalid_number)
                inputVolumes == null -> getString(R.string.invalid_number)
                (numVolumes != 0 && inputVolumes > numVolumes) -> getString(R.string.invalid_number)
                else -> null
            }
        }
    }

    private fun syncListStatus() {
        binding.chaptersField.setText(myListStatus?.numChaptersRead.toString())
        binding.volumesField.setText(myListStatus?.numVolumesRead.toString())
        binding.statusField.setText(myListStatus?.status.formatListStatus(safeContext), false)
        binding.scoreSlider.value = myListStatus?.score?.toFloat() ?: 0f
        DateUtils.getLocalDateFromDateString(myListStatus?.startDate)?.let {
            binding.startDateField.setText(DateUtils.formatLocalDateToString(it))
        }
        DateUtils.getLocalDateFromDateString(myListStatus?.endDate)?.let {
            binding.endDateField.setText(DateUtils.formatLocalDateToString(it))
        }
        binding.rereadField.setText(myListStatus?.numTimesReread.toString())
    }

}