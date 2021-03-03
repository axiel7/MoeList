package com.axiel7.moelist.ui.details

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doOnTextChanged
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.R
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.utils.InsetsHelper
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_anime_details.*
import kotlinx.android.synthetic.main.bottom_sheet_edit_anime.*
import kotlinx.android.synthetic.main.bottom_sheet_edit_anime.score_text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditAnimeFragment(private var myListStatus: MyListStatus?,
                        private var animeId: Int,
                        private var numEpisodes: Int) : BottomSheetDialogFragment() {
    private var entryUpdated: Boolean = false
    private lateinit var dataPasser: OnDataPass

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_edit_anime, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Set peek height to hide delete button
        InsetsHelper.getViewBottomHeight(
            view as ViewGroup,
            R.id.divider,
            (dialog as BottomSheetDialog).behavior
        )

        loading.hide()
        apply_button?.setOnClickListener {

            var status :String? = null
            val statusCurrent = StringFormat.formatListStatusInverted(status_field.text.toString(), requireContext())
            val statusOrigin = myListStatus?.status

            var score :Int? = null
            val scoreCurrent = score_slider.value.toInt()
            val scoreOrigin = myListStatus?.score

            var episodes: Int? = null
            val episodesCurrent = episodes_field.text.toString().toIntOrNull()
            val episodesOrigin = myListStatus?.num_episodes_watched
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
                episodes = numEpisodes
            }
            initUpdateCall(status, score, episodes)
        }

        cancel_button?.setOnClickListener {
            syncListStatus()
            dismiss()
        }

        val statusItems = listOf(getString(R.string.watching), getString(R.string.completed),
            getString(R.string.on_hold), getString(R.string.dropped), getString(R.string.ptw))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_status_item, statusItems)
        (status_layout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        score_slider.addOnChangeListener { _, value, _ ->
            val scoreTextValue = "${getString(R.string.score_value)} " + value.toInt().let { StringFormat.formatScore(it, requireContext()) }
            score_text?.text = scoreTextValue
        }
        val scoreTextValue = "${getString(R.string.score_value)} " + score_slider.value.toInt().let { StringFormat.formatScore(it, requireContext()) }
        score_text?.text = scoreTextValue

        delete_button?.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.delete))
                .setMessage(resources.getString(R.string.delete_confirmation))
                .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                    // Respond to neutral button press
                }
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    // Respond to positive button press
                    deleteEntry()
                }
                .show()
        }

        episodes_field_layout.suffixText = "/$numEpisodes"
        minus_button.setOnClickListener {
            val inputEpisodes = episodes_field.text.toString().toIntOrNull() ?: 0
            if (inputEpisodes > 0) {
                episodes_field.setText((inputEpisodes - 1).toString())
            }
        }
        plus_button.setOnClickListener {
            val inputEpisodes = episodes_field.text.toString().toIntOrNull() ?: 0
            if (inputEpisodes < numEpisodes || numEpisodes == 0) {
                episodes_field.setText((inputEpisodes + 1).toString())
            }
        }

        if (myListStatus != null) {
            syncListStatus()
        }
        //episodes input logic
        episodes_field_layout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputEpisodes = text.toString().toIntOrNull()
            if (numEpisodes!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputEpisodes==null
                    || inputEpisodes > numEpisodes) {
                    episodes_field_layout.error = getString(R.string.invalid_number)
                } else { episodes_field_layout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    episodes_field_layout.error = getString(R.string.invalid_number)
                } else { episodes_field_layout.error = null }
            }
        }
    }

    private fun initUpdateCall(status: String?, score: Int?, watchedEpisodes: Int?) {
        val shouldNotUpdate = status.isNullOrEmpty() && score==null && watchedEpisodes==null
        if (!shouldNotUpdate) {
            loading.show()
            val updateListCall = MyApplication.malApiService
                .updateAnimeList(Urls.apiBaseUrl + "anime/$animeId/my_list_status", status, score, watchedEpisodes)
            patchCall(updateListCall)
        }
    }
    private fun patchCall(call: Call<MyListStatus>) {
        call.enqueue(object :Callback<MyListStatus> {
            override fun onResponse(call: Call<MyListStatus>, response: Response<MyListStatus>) {
                if (response.isSuccessful && isAdded) {
                    syncListStatus()
                    myListStatus = response.body()
                    entryUpdated = true
                    val toastText = getString(R.string.updated)
                    Snackbar.make(edit_sheet, toastText, Snackbar.LENGTH_SHORT).show()
                    loading.hide()
                    this@EditAnimeFragment.dismiss()
                }
                else if (isAdded) {
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                if (isAdded) {
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun deleteEntry() {
        val deleteCall = MyApplication.malApiService.deleteEntry(Urls.apiBaseUrl + "anime/$animeId/my_list_status")
        deleteCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful && isAdded) {
                    myListStatus = null
                    //changeFabAction()
                    entryUpdated = true
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.deleted), Snackbar.LENGTH_SHORT).show()
                    this@EditAnimeFragment.dismiss()
                }
                else if (isAdded) {
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.error_delete_entry), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                if (isAdded) {
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun syncListStatus() {
        val watchedEpisodes = myListStatus?.num_episodes_watched
        episodes_field.setText(watchedEpisodes.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus?.status, requireContext())
        status_field.setText(statusValue, false)

        score_slider.value = myListStatus?.score?.toFloat() ?: 0f
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        syncListStatus()
        dataPasser.onAnimeEntryUpdated(entryUpdated)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }

    interface OnDataPass {
        fun onAnimeEntryUpdated(updated: Boolean)
    }

}