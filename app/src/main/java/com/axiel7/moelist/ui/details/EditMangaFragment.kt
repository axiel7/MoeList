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
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.BottomSheetEditMangaBinding
import com.axiel7.moelist.model.MyMangaListStatus
import com.axiel7.moelist.utils.InsetsHelper
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMangaFragment(private var myListStatus: MyMangaListStatus?,
                        private var mangaId: Int,
                        private var numChapters: Int,
                        private var numVolumes: Int,
                        private var position: Int) : BottomSheetDialogFragment() {
    private var entryUpdated: Boolean = false
    private lateinit var dataPasser: OnDataPass
    private var _binding: BottomSheetEditMangaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditMangaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Set peek height to hide delete button
        InsetsHelper.getViewBottomHeight(
            view as ViewGroup,
            R.id.divider,
            (dialog as BottomSheetDialog).behavior
        )

        if (isAdded) {
            binding.loading.hide()
        }
        binding.applyButton.setOnClickListener {

            var status :String? = null
            val statusCurrent = StringFormat.formatListStatusInverted(binding.statusField.text.toString(), requireContext())
            val statusOrigin = myListStatus?.status

            var score :Int? = null
            val scoreCurrent = binding.scoreSlider.value.toInt()
            val scoreOrigin = myListStatus?.score

            var chapters: Int? = null
            val chaptersCurrent = binding.chaptersField.text.toString().toIntOrNull()
            val chaptersOrigin = myListStatus?.num_chapters_read
            var volumes: Int? = null
            val volumesCurrent = binding.volumesField.text.toString().toIntOrNull()
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
        binding.cancelButton.setOnClickListener {
            syncListStatus()
            dismiss()
        }

        val statusItems = listOf(getString(R.string.reading), getString(R.string.completed),
            getString(R.string.on_hold), getString(R.string.dropped), getString(R.string.ptr))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_status_item, statusItems)
        (binding.statusLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.scoreSlider.addOnChangeListener { _, value, _ ->
            val scoreTextValue = "${getString(R.string.score_value)} " +
                    value.toInt().let { StringFormat.formatScore(it, requireContext()) }
            binding.scoreText.text = scoreTextValue
        }
        val scoreTextValue = "${getString(R.string.score_value)} " +
                binding.scoreSlider.value.toInt().let { StringFormat.formatScore(it, requireContext()) }
        binding.scoreText.text = scoreTextValue

        binding.deleteButton.setOnClickListener {
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
        }

        if (myListStatus != null) {
            syncListStatus()
        }
        // chapters/volumes input logic
        binding.chaptersLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputChapters = text.toString().toIntOrNull()
            if (numChapters!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputChapters==null
                    || inputChapters > numChapters) {
                    binding.chaptersLayout.error = getString(R.string.invalid_number)
                } else { binding.chaptersLayout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    binding.chaptersLayout.error = getString(R.string.invalid_number)
                } else { binding.chaptersLayout.error = null }
            }
        }
        binding.volumesLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputVolumes = text.toString().toIntOrNull()
            if (numVolumes!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputVolumes==null
                    || inputVolumes > numVolumes) {
                    binding.volumesLayout.error = getString(R.string.invalid_number)
                } else { binding.volumesLayout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    binding.volumesLayout.error = getString(R.string.invalid_number)
                } else { binding.volumesLayout.error = null }
            }
        }
    }

    private fun initUpdateCall(status: String?, score: Int?, chaptersRead: Int?, volumesRead: Int?) {
        val shouldNotUpdate = status.isNullOrEmpty() && score==null && chaptersRead==null && volumesRead==null
        if (!shouldNotUpdate && isAdded) {
            binding.loading.show()
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
                    Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()
                    binding.loading.hide()
                    this@EditMangaFragment.dismiss()
                }
                else if (isAdded) {
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.error_updating_list), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyMangaListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                if (isAdded) {
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.error_server), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun deleteEntry() {
        val deleteCall = MyApplication.malApiService.deleteEntry(Urls.apiBaseUrl + "manga/$mangaId/my_list_status")
        deleteCall.enqueue(object :Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful && isAdded) {
                    myListStatus = null
                    entryUpdated = true
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                else if (isAdded) {
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.error_delete_entry), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                if (isAdded) {
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.error_server), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun syncListStatus() {
        val chaptersRead = myListStatus?.num_chapters_read
        binding.chaptersField.setText(chaptersRead.toString())
        val volumesRead = myListStatus?.num_volumes_read
        binding.volumesField.setText(volumesRead.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus?.status, requireContext())
        binding.statusField.setText(statusValue, false)

        binding.scoreSlider.value = myListStatus?.score?.toFloat() ?: 0f
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        syncListStatus()
        dataPasser.onMangaEntryUpdated(entryUpdated, position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnDataPass {
        fun onMangaEntryUpdated(updated: Boolean, position: Int)
    }

}