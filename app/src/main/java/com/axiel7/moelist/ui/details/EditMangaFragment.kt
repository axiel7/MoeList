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
import com.axiel7.moelist.model.MyMangaListStatus
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.bottom_sheet_edit_manga.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMangaFragment(private var myListStatus: MyMangaListStatus?,
                        private var mangaId: Int,
                        private var numChapters: Int,
                        private var numVolumes: Int) : BottomSheetDialogFragment() {
    private var entryUpdated: Boolean = false
    private lateinit var dataPasser: OnDataPass

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_edit_manga, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading.hide()
        apply_button?.setOnClickListener {

            var status :String? = null
            val statusCurrent = StringFormat.formatListStatusInverted(status_field.text.toString(), requireContext())
            val statusOrigin = myListStatus?.status

            var score :Int? = null
            val scoreCurrent = score_slider.value.toInt()
            val scoreOrigin = myListStatus?.score

            var chapters: Int? = null
            val chaptersCurrent = chapters_field.text.toString().toIntOrNull()
            val chaptersOrigin = myListStatus?.num_chapters_read
            var volumes: Int? = null
            val volumesCurrent = volumes_field.text.toString().toIntOrNull()
            val volumesOrigin = myListStatus?.num_volumes_read
            if (statusCurrent!=statusOrigin) {
                status = statusCurrent
            }
            if (scoreCurrent!=scoreOrigin) {
                score = scoreCurrent
            }
            if (chaptersCurrent!=chaptersOrigin) {
                chapters = chaptersCurrent
            }
            if (volumesCurrent!=volumesOrigin) {
                volumes = volumesCurrent
            }
            if (status=="completed") {
                chapters = numChapters
                volumes = numVolumes
            }
            initUpdateCall(status, score, chapters, volumes)
        }
        cancel_button?.setOnClickListener {
            syncListStatus()
            dismiss()
        }

        val statusItems = listOf(getString(R.string.reading), getString(R.string.completed),
            getString(R.string.on_hold), getString(R.string.dropped), getString(R.string.ptr))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_status_item, statusItems)
        (status_layout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        score_slider.addOnChangeListener { _, value, _ ->
            val scoreTextValue = "${getString(R.string.score_value)} " +
                    value.toInt().let { StringFormat.formatScore(it, requireContext()) }
            score_text?.text = scoreTextValue
        }
        val scoreTextValue = "${getString(R.string.score_value)} " +
                score_slider.value.toInt().let { StringFormat.formatScore(it, requireContext()) }
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
        chapters_layout.suffixText = "/$numChapters"
        volumes_layout.suffixText = "/$numVolumes"
        if (myListStatus != null) {
            syncListStatus()
        }
        // chapters/volumes input logic
        chapters_layout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputChapters = text.toString().toIntOrNull()
            if (numChapters!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputChapters==null
                    || inputChapters > numChapters) {
                    chapters_layout.error = getString(R.string.invalid_number)
                } else { chapters_layout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    chapters_layout.error = getString(R.string.invalid_number)
                } else { chapters_layout.error = null }
            }
        }
        volumes_layout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputVolumes = text.toString().toIntOrNull()
            if (numVolumes!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputVolumes==null
                    || inputVolumes > numVolumes) {
                    volumes_layout.error = getString(R.string.invalid_number)
                } else { volumes_layout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    volumes_layout.error = getString(R.string.invalid_number)
                } else { volumes_layout.error = null }
            }
        }
    }

    private fun initUpdateCall(status: String?, score: Int?, chaptersRead: Int?, volumesRead: Int?) {
        val shouldNotUpdate = status.isNullOrEmpty() && score==null && chaptersRead==null && volumesRead==null
        if (!shouldNotUpdate) {
            loading.show()
            val updateListCall = MyApplication.malApiService
                .updateMangaList(Urls.apiBaseUrl + "manga/$mangaId/my_list_status", status, score, chaptersRead, volumesRead)
            patchCall(updateListCall)
        }
    }
    private fun patchCall(call: Call<MyMangaListStatus>) {
        call.enqueue(object :Callback<MyMangaListStatus> {
            override fun onResponse(call: Call<MyMangaListStatus>, response: Response<MyMangaListStatus>) {
                if (response.isSuccessful && isAdded) {
                    syncListStatus()
                    myListStatus = response.body()
                    entryUpdated = true
                    val toastText = getString(R.string.updated)
                    Snackbar.make(edit_sheet, toastText, Snackbar.LENGTH_SHORT).show()
                    loading.hide()
                    this@EditMangaFragment.dismiss()
                }
                else if (isAdded) {
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyMangaListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                if (isAdded) {
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun deleteEntry() {
        val deleteCall = MyApplication.malApiService.deleteEntry(Urls.apiBaseUrl + "manga/$mangaId/my_list_status")
        deleteCall.enqueue(object :Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    myListStatus = null
                    entryUpdated = true
                    loading.hide()
                    Snackbar.make(edit_sheet, getString(R.string.deleted), Snackbar.LENGTH_SHORT).show()
                    dismiss()
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
        val chaptersRead = myListStatus?.num_chapters_read
        chapters_field.setText(chaptersRead.toString())
        val volumesRead = myListStatus?.num_volumes_read
        volumes_field.setText(volumesRead.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus?.status, requireContext())
        status_field.setText(statusValue, false)

        score_slider.value = myListStatus?.score?.toFloat() ?: 0f
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        syncListStatus()
        dataPasser.onMangaEntryUpdated(entryUpdated)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }

    interface OnDataPass {
        fun onMangaEntryUpdated(updated: Boolean)
    }

}