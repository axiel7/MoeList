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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import coil.load
import coil.transform.CircleCropTransformation
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
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

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var userAnimeStatistics: UserAnimeStatistics
    private lateinit var profilePicture: ImageView
    private lateinit var usernameView: TextView
    private lateinit var locationView: TextView
    private lateinit var birthdayView: TextView
    private lateinit var joinedView: TextView
    private lateinit var animeChart: DonutProgressView
    private lateinit var watchingText: TextView
    private lateinit var completedText: TextView
    private lateinit var onHoldText: TextView
    private lateinit var droppedText: TextView
    private lateinit var ptwText: TextView
    private lateinit var totalText: TextView
    private lateinit var daysText: TextView
    private lateinit var episodesText: TextView
    private lateinit var scoreText: TextView
    private lateinit var rewatchText: TextView
    private lateinit var viewOnMAL: Button
    private lateinit var snackBarView: View
    private var user: User? = null
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //SharedPrefsHelpers.init(context)
        sharedPref = SharedPrefsHelpers.instance!!

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

        snackBarView = view
        profilePicture = view.findViewById(R.id.profile_picture)
        usernameView = view.findViewById(R.id.username)
        locationView = view.findViewById(R.id.location)
        birthdayView = view.findViewById(R.id.birthday)
        joinedView = view.findViewById(R.id.joined_at)
        animeChart = view.findViewById(R.id.anime_chart)
        watchingText = view.findViewById(R.id.watching_text)
        completedText = view.findViewById(R.id.completed_text)
        onHoldText = view.findViewById(R.id.onhold_text)
        droppedText = view.findViewById(R.id.dropped_text)
        ptwText = view.findViewById(R.id.ptw_text)
        totalText = view.findViewById(R.id.total_entries)
        daysText = view.findViewById(R.id.days_wasted)
        episodesText = view.findViewById(R.id.total_episodes)
        scoreText = view.findViewById(R.id.mean_score)
        rewatchText = view.findViewById(R.id.rewatched)
        viewOnMAL = view.findViewById(R.id.view_on_mal)
        if (user!=null) {
            setDataToViews()
        }

        getUser()
    }
    private fun getUser() {
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
                        if (isAdded) { setDataToViews() }
                    }
                }
                else if (response.code()==401) {
                    if (isAdded) {
                        Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }
    @SuppressLint("NewApi")
    private fun setDataToViews() {

        profilePicture
            .load(user?.picture) {
                crossfade(true)
                crossfade(500)
                transformations(CircleCropTransformation())
                error(R.drawable.ic_round_account_circle_24)
            }
        profilePicture.setOnClickListener {
            val intent = Intent(context, FullPosterActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                profilePicture,
                "shared_poster_container"
            )
            intent.putExtra("posterUrl", user?.picture)
            startActivity(intent, options.toBundle())
        }

        val username = user?.name
        usernameView.text = username

        val location = user?.location
        if (location.isNullOrEmpty()) {
            locationView.visibility = View.GONE
        } else {
            locationView.visibility = View.VISIBLE
            locationView.text = location
        }

        val birthday = user?.birthday
        if (birthday.isNullOrEmpty()) {
            birthdayView.visibility = View.GONE
        } else {
            birthdayView.visibility = View.VISIBLE
            //birthdayView.text = LocalDate.parse(birthday).toString()
        }
        joinedView.text = LocalDate.parse(user?.joined_at, DateTimeFormatter.ISO_DATE_TIME).toString()

        val watching = userAnimeStatistics.num_items_watching!!
        val completed = userAnimeStatistics.num_items_completed!!
        val onHold = userAnimeStatistics.num_items_on_hold!!
        val dropped = userAnimeStatistics.num_items_dropped!!
        val ptw = userAnimeStatistics.num_items_plan_to_watch!!
        val totalEntries = userAnimeStatistics.num_items!!

        val watchingKey = "${getString(R.string.watching)} ($watching)"
        watchingText.text = watchingKey
        val completedKey = "${getString(R.string.completed)} ($completed)"
        completedText.text = completedKey
        val onHoldKey = "${getString(R.string.on_hold)} ($onHold)"
        onHoldText.text = onHoldKey
        val droppedKey = "${getString(R.string.dropped)} ($dropped)"
        droppedText.text = droppedKey
        val ptwKey = "${getString(R.string.ptw)} ($ptw)"
        ptwText.text = ptwKey
        val totalKey = "${getString(R.string.total_entries)} $totalEntries"
        totalText.text = totalKey

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
        animeChart.cap = 0f
        animeChart.submitData(listOf(ptwSection, droppedSection, onHoldSection, completedSection, watchingSection))

        val days = userAnimeStatistics.num_days!!
        val episodes = NumberFormat.getInstance().format(userAnimeStatistics.num_episodes)!!
        val score = userAnimeStatistics.mean_score!!
        val rewatch = NumberFormat.getInstance().format(userAnimeStatistics.num_times_rewatched)!!

        val daysValue = "$days\n${getString(R.string.days)}"
        val episodesValue = "$episodes\n${getString(R.string.episodes)}"
        val scoreValue = "$score\n${getString(R.string.mean_score)}"
        val rewatchValue = "$rewatch\n${getString(R.string.rewatched)}"

        daysText.text = daysValue
        episodesText.text = episodesValue
        scoreText.text = scoreValue
        rewatchText.text = rewatchValue

        viewOnMAL.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://myanimelist.net/profile/$username"))
            startActivity(intent)
        }
    }
}