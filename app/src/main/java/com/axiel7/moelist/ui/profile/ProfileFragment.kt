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
import com.axiel7.moelist.model.User
import com.axiel7.moelist.model.UserAnimeStatistics
import com.axiel7.moelist.ui.details.FullPosterActivity
import com.axiel7.moelist.utils.ResponseConverter
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_profile.*
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
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
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
                    Snackbar.make(profile_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }
    @SuppressLint("NewApi")
    private fun setDataToViews() {

        profile_picture
            .load(user?.picture) {
                crossfade(true)
                crossfade(500)
                transformations(CircleCropTransformation())
                error(R.drawable.ic_round_account_circle_24)
            }
        profile_picture.setOnClickListener {
            val intent = Intent(context, FullPosterActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                profile_picture,
                "shared_poster_container"
            )
            intent.putExtra("posterUrl", user?.picture)
            startActivity(intent, options.toBundle())
        }

        val usernameText = user?.name
        username.text = usernameText

        val locationText = user?.location
        if (locationText.isNullOrEmpty()) {
            location.visibility = View.GONE
        } else {
            location.visibility = View.VISIBLE
            location.text = locationText
        }

        val birthdayText = user?.birthday
        if (birthdayText.isNullOrEmpty()) {
            birthday.visibility = View.GONE
        } else {
            birthday.visibility = View.VISIBLE
            birthday.text = birthdayText
        }
        joined_at.text = LocalDate.parse(user?.joined_at, DateTimeFormatter.ISO_DATE_TIME).toString()

        val watching = userAnimeStatistics.num_items_watching!!
        val completed = userAnimeStatistics.num_items_completed!!
        val onHold = userAnimeStatistics.num_items_on_hold!!
        val dropped = userAnimeStatistics.num_items_dropped!!
        val ptw = userAnimeStatistics.num_items_plan_to_watch!!
        val totalEntries = userAnimeStatistics.num_items!!

        val watchingKey = "${getString(R.string.watching)} ($watching)"
        watching_text.text = watchingKey
        val completedKey = "${getString(R.string.completed)} ($completed)"
        completed_text.text = completedKey
        val onHoldKey = "${getString(R.string.on_hold)} ($onHold)"
        onhold_text.text = onHoldKey
        val droppedKey = "${getString(R.string.dropped)} ($dropped)"
        dropped_text.text = droppedKey
        val ptwKey = "${getString(R.string.ptw)} ($ptw)"
        ptw_text.text = ptwKey
        val totalKey = "${getString(R.string.total_entries)} $totalEntries"
        total_entries.text = totalKey

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
        anime_chart.cap = 0f
        anime_chart.submitData(listOf(ptwSection, droppedSection, onHoldSection, completedSection, watchingSection))

        val days = userAnimeStatistics.num_days!!
        val episodes = NumberFormat.getInstance().format(userAnimeStatistics.num_episodes)!!
        val score = userAnimeStatistics.mean_score!!
        val rewatch = NumberFormat.getInstance().format(userAnimeStatistics.num_times_rewatched)!!

        val daysValue = "$days\n${getString(R.string.days)}"
        val episodesValue = "$episodes\n${getString(R.string.episodes)}"
        val scoreValue = "$score\n${getString(R.string.mean_score)}"
        val rewatchValue = "$rewatch\n${getString(R.string.rewatched)}"

        days_wasted.text = daysValue
        total_episodes.text = episodesValue
        mean_score.text = scoreValue
        rewatched.text = rewatchValue

        view_on_mal.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://myanimelist.net/profile/$usernameText"))
            startActivity(Intent.createChooser(intent, view_on_mal.text))
        }
    }
}