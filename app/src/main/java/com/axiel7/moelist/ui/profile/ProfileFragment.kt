package com.axiel7.moelist.ui.profile

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.futured.donut.DonutSection
import coil.load
import coil.transform.CircleCropTransformation
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.UseCases
import com.axiel7.moelist.databinding.FragmentProfileBinding
import com.axiel7.moelist.model.User
import com.axiel7.moelist.model.UserAnimeStatistics
import com.axiel7.moelist.ui.details.FullPosterActivity
import com.axiel7.moelist.utils.ResponseConverter
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProfileFragment : Fragment() {

    private var sharedPref = SharedPrefsHelpers.instance!!
    private lateinit var userAnimeStatistics: UserAnimeStatistics
    private var user: User? = null
    private var userId = -1
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //sharedPref = SharedPrefsHelpers.instance!!

        userId = sharedPref.getInt("userId", -1)
        if (MyApplication.animeDb?.userDao()?.getUserById(userId)!=null) {
            user = MyApplication.animeDb?.userDao()?.getUserById(userId)!!
            userAnimeStatistics = user?.anime_statistics!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (user!=null) {
            setDataToViews()
        }

        getUser()
    }
    fun getUser() {
        val call = malApiService.getUserInfo("id,name,gender,location,joined_at,anime_statistics")
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val responseOld = ResponseConverter
                        .stringToUserResponse(sharedPref.getString("userResponse", ""))

                    if (responseOld!=response.body() || user==null) {
                        user = response.body()
                        sharedPref.saveString("userResponse",
                            ResponseConverter.userResponseToString(user))
                        userAnimeStatistics = user!!.anime_statistics
                        userId = user!!.id
                        MyApplication.animeDb?.userDao()?.insertUser(user!!)
                        sharedPref.saveInt("userId", userId)
                        sharedPref.saveString("userPicture", user!!.picture)
                        if (isAdded) { setDataToViews() }
                    }
                }

                else if (response.code()==401) {
                    if (isAdded) {
                        UseCases.logOut(requireContext())
                    }
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }
    @SuppressLint("NewApi")
    private fun setDataToViews() {

        binding.profilePicture.load(user?.picture) {
                crossfade(true)
                crossfade(500)
                transformations(CircleCropTransformation())
                error(R.drawable.ic_round_account_circle_24)
            }
        binding.profilePicture.setOnClickListener {
            val intent = Intent(context, FullPosterActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                it,
                "shared_poster_container"
            )
            intent.putExtra("posterUrl", user?.picture)
            startActivity(intent, options.toBundle())
        }

        val usernameText = user?.name
        binding.username.text = usernameText

        val locationText = user?.location
        if (locationText.isNullOrEmpty()) {
            binding.location.visibility = View.GONE
        } else {
            binding.location.visibility = View.VISIBLE
            binding.location.text = locationText
        }

        val birthdayText = user?.birthday
        if (birthdayText.isNullOrEmpty()) {
            binding.birthday.visibility = View.GONE
        } else {
            binding.birthday.visibility = View.VISIBLE
            binding.birthday.text = birthdayText
        }
        binding.joinedAt.text = LocalDate.parse(user?.joined_at, DateTimeFormatter.ISO_DATE_TIME).toString()

        val watching = userAnimeStatistics.num_items_watching!!
        val completed = userAnimeStatistics.num_items_completed!!
        val onHold = userAnimeStatistics.num_items_on_hold!!
        val dropped = userAnimeStatistics.num_items_dropped!!
        val ptw = userAnimeStatistics.num_items_plan_to_watch!!
        val totalEntries = userAnimeStatistics.num_items!!

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

        val days = userAnimeStatistics.num_days!!
        val episodes = NumberFormat.getInstance().format(userAnimeStatistics.num_episodes)!!
        val score = userAnimeStatistics.mean_score!!
        val rewatch = NumberFormat.getInstance().format(userAnimeStatistics.num_times_rewatched)!!

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}