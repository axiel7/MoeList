package com.axiel7.moelist.ui.profile

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.futured.donut.DonutSection
import coil.load
import coil.transform.CircleCropTransformation
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.databinding.FragmentProfileBinding
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.details.FullPosterFragment
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.google.android.material.transition.MaterialFade
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProfileBinding
        get() = FragmentProfileBinding::inflate
    private val viewModel: ProfileViewModel by viewModels()
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFade()
        exitTransition = MaterialFade()
        userId = sharedPref.getInt("userId", -1)
    }

    override fun setup() {
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        launchLifecycleStarted {
            viewModel.user.collectLatest {
                it?.let { setUserData(it) }
            }
        }

        launchLifecycleStarted {
            viewModel.response.collectLatest {
                if (it.first == RESPONSE_ERROR) {
                    showSnackbar(it.second)
                }
            }
        }

        viewModel.getUser(userId)
    }

    private fun setUserData(user: User) {
        sharedPref.saveInt("userId", user.id)

        binding.profilePicture.load(user.picture) {
                transformations(CircleCropTransformation())
                error(R.drawable.ic_round_account_circle_24)
            }

        binding.profilePicture.setOnClickListener {
            mainActivity?.navigate(
                idAction = R.id.action_global_fullPosterFragment,
                bundle = Bundle().apply { putString("poster_url", user.picture) }
            )
        }

        val usernameText = user.name
        binding.username.text = usernameText

        val locationText = user.location
        if (locationText.isNullOrEmpty()) {
            binding.location.visibility = View.GONE
        } else {
            binding.location.visibility = View.VISIBLE
            binding.location.text = locationText
        }

        val birthdayText = user.birthday
        if (birthdayText.isNullOrEmpty()) {
            binding.birthday.visibility = View.GONE
        } else {
            binding.birthday.visibility = View.VISIBLE
            binding.birthday.text = birthdayText
        }
        binding.joinedAt.text = LocalDate.parse(user.joinedAt, DateTimeFormatter.ISO_DATE_TIME).toString()

        val watching = user.animeStatistics.numItemsWatching ?: 0f
        val completed = user.animeStatistics.numItemsCompleted ?: 0f
        val onHold = user.animeStatistics.numItemsOnHold ?: 0f
        val dropped = user.animeStatistics.numItemsDropped ?: 0f
        val ptw = user.animeStatistics.numItemsPlanToWatch ?: 0f
        val totalEntries = user.animeStatistics.numItems ?: 0f

        val watchingKey = "${getString(R.string.watching)} ($watching)"
        binding.watchingText.text = watchingKey
        val completedKey = "${getString(R.string.completed)} ($completed)"
        binding.completedText.text = completedKey
        val onHoldKey = "${getString(R.string.on_hold)} ($onHold)"
        binding.onholdText.text = onHoldKey
        val droppedKey = "${getString(R.string.dropped)} ($dropped)"
        binding.droppedText.text = droppedKey
        val ptwKey = "${getString(R.string.ptw)} ($ptw)"
        binding.ptwText.text = ptwKey
        val totalKey = "${getString(R.string.total_entries)} $totalEntries"
        binding.totalEntries.text = totalKey

        val watchingSection = DonutSection(
            name = "Watching",
            color = Color.parseColor("#00c853"),
            amount = watching.toFloat()
        )
        val completedSection = DonutSection(
            name = "Completed",
            color = Color.parseColor("#5c6bc0"),
            amount = completed.toFloat()
        )
        val onHoldSection = DonutSection(
            name = "On Hold",
            color = Color.parseColor("#ffd600"),
            amount = onHold.toFloat()
        )
        val droppedSection = DonutSection(
            name = "Dropped",
            color = Color.parseColor("#d50000"),
            amount = dropped.toFloat()
        )
        val ptwSection = DonutSection(
            name = "Plan to Watch",
            color = Color.parseColor("#9e9e9e"),
            amount = ptw.toFloat()
        )
        binding.animeChart.cap = 0f
        binding.animeChart.submitData(listOf(ptwSection, droppedSection, onHoldSection, completedSection, watchingSection))

        val days = user.animeStatistics.numDays ?: 0
        val episodes = NumberFormat.getInstance().format(user.animeStatistics.numEpisodes ?: 0)
        val score = user.animeStatistics.meanScore ?: 0
        val rewatch = NumberFormat.getInstance().format(user.animeStatistics.numTimesRewatched ?: 0)

        val daysValue = "$days\n${getString(R.string.days)}"
        val episodesValue = "$episodes\n${getString(R.string.episodes)}"
        val scoreValue = "$score\n${getString(R.string.mean_score)}"
        val rewatchValue = "$rewatch\n${getString(R.string.rewatched)}"

        binding.daysWasted.text = daysValue
        binding.totalEpisodes.text = episodesValue
        binding.meanScore.text = scoreValue
        binding.rewatched.text = rewatchValue

        binding.viewOnMal.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://myanimelist.net/profile/$usernameText"))
            startActivity(Intent.createChooser(intent, binding.viewOnMal.text))
        }
    }
}