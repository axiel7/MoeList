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
import com.axiel7.moelist.databinding.BottomSheetEditAnimeBinding
import com.axiel7.moelist.model.MyListStatus
import com.axiel7.moelist.utils.InsetsHelper
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditAnimeFragment(private var myListStatus: MyListStatus?,
                        private var animeId: Int,
                        private var numEpisodes: Int,
                        private var position: Int) : BottomSheetDialogFragment() {
    private var entryUpdated: Boolean = false
    private lateinit var dataPasser: OnDataPass
    private var _binding: BottomSheetEditAnimeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditAnimeBinding.inflate(inflater, container, false)
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

            var episodes: Int? = null
            val episodesCurrent = binding.episodesField.text.toString().toIntOrNull()
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

        binding.cancelButton.setOnClickListener {
            syncListStatus()
            dismiss()
        }

        val statusItems = listOf(getString(R.string.watching), getString(R.string.completed),
            getString(R.string.on_hold), getString(R.string.dropped), getString(R.string.ptw))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_status_item, statusItems)
        (binding.statusLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.scoreSlider.addOnChangeListener { _, value, _ ->
            val scoreTextValue = "${getString(R.string.score_value)} " + value.toInt().let { StringFormat.formatScore(it, requireContext()) }
            binding.scoreText.text = scoreTextValue
        }
        val scoreTextValue = "${getString(R.string.score_value)} " + binding.scoreSlider.value.toInt().let { StringFormat.formatScore(it, requireContext()) }
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
        }
        //episodes input logic
        binding.episodesFieldLayout.editText?.doOnTextChanged { text, _, _, _ ->
            val inputEpisodes = text.toString().toIntOrNull()
            if (numEpisodes!=0) {
                if (text.isNullOrEmpty() || text.isBlank() || inputEpisodes==null
                    || inputEpisodes > numEpisodes) {
                    binding.episodesFieldLayout.error = getString(R.string.invalid_number)
                } else { binding.episodesFieldLayout.error = null }
            }
            else {
                if (text.isNullOrEmpty() || text.isBlank()) {
                    binding.episodesFieldLayout.error = getString(R.string.invalid_number)
                } else { binding.episodesFieldLayout.error = null }
            }
        }
    }

    private fun initUpdateCall(status: String?, score: Int?, watchedEpisodes: Int?) {
        val shouldNotUpdate = status.isNullOrEmpty() && score==null && watchedEpisodes==null
        if (!shouldNotUpdate && isAdded) {
            binding.loading.show()
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
                    Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()
                    binding.loading.hide()
                    this@EditAnimeFragment.dismiss()
                }
                else if (isAdded) {
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.error_updating_list), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyListStatus>, t: Throwable) {
                Log.d("MoeLog", t.toString())
                if (isAdded) {
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.error_server), Toast.LENGTH_SHORT).show()
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
                    binding.loading.hide()
                    Toast.makeText(requireContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show()
                    this@EditAnimeFragment.dismiss()
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
        val watchedEpisodes = myListStatus?.num_episodes_watched
        binding.episodesField.setText(watchedEpisodes.toString())

        val statusValue = StringFormat.formatListStatus(myListStatus?.status, requireContext())
        binding.statusField.setText(statusValue, false)

        binding.scoreSlider.value = myListStatus?.score?.toFloat() ?: 0f
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        syncListStatus()
        dataPasser.onAnimeEntryUpdated(entryUpdated, position)
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
        fun onAnimeEntryUpdated(updated: Boolean, position: Int)
    }

}