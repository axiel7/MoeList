package com.axiel7.moelist.ui.seasonal

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.BottomSheetSeasonalBinding
import com.axiel7.moelist.ui.base.BaseBottomSheetDialogFragment
import com.axiel7.moelist.utils.SeasonCalendar
import com.axiel7.moelist.utils.StringExtensions.formatSeason
import com.axiel7.moelist.utils.StringExtensions.formatSeasonInverted

class SeasonalFilterFragment : BaseBottomSheetDialogFragment<BottomSheetSeasonalBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetSeasonalBinding
        get() = BottomSheetSeasonalBinding::inflate
    private val viewModel: SeasonalViewModel by viewModels()
    private val seasons: Array<String> by lazy {
        arrayOf(getString(R.string.winter),
            getString(R.string.spring),
            getString(R.string.summer),
            getString(R.string.fall)
        )
    }
    private val adapterSeason: ArrayAdapter<String> by lazy {
        ArrayAdapter(safeContext, R.layout.item_spinner, seasons)
    }
    private val years: Array<Int> by lazy {
        (BASE_YEAR..SeasonCalendar.currentYear+1).toList().sortedDescending().toTypedArray()
    }
    private val adapterYear: ArrayAdapter<Int> by lazy {
        ArrayAdapter(safeContext, R.layout.item_spinner, years)
    }
    private val selectedSeason get() = binding.seasonField.text.toString()
    private val selectedYear get() = binding.yearField.text.toString().toInt()

    override fun setup() {
        binding.applyButton.setOnClickListener {
            (parentFragment as? SeasonalFragment)?.changeSeason(
                year = selectedYear,
                season = selectedSeason.formatSeasonInverted(safeContext)
            )
            dismiss()
        }

        binding.cancelButton.setOnClickListener { dismiss() }

        binding.seasonField.setAdapter(adapterSeason)
        binding.yearField.setAdapter(adapterYear)

        binding.seasonField.setText(
            viewModel.startSeason.value.season.formatSeason(safeContext),
            false
        )
        binding.yearField.setText(viewModel.startSeason.value.year.toString(), false)
    }

    companion object {
        const val BASE_YEAR = 1917
    }
}