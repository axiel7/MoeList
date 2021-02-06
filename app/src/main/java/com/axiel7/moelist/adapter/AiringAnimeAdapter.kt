@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection")

package com.axiel7.moelist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.utils.SeasonCalendar
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

class AiringAnimeAdapter(private val animes: MutableList<SeasonalList>,
                         private val rowLayout: Int,
                         private val context: Context,
                         private val onClickListener: (View, SeasonalList) -> Unit) :
    RecyclerView.Adapter<AiringAnimeAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val animeTitle: TextView = v.findViewById(R.id.anime_title)
        val animePoster: ImageView = v.findViewById(R.id.anime_poster)
        val airingText: TextView = v.findViewById(R.id.airing_time)
        val scoreText: TextView = v.findViewById(R.id.score_text)

        init {
            animePoster.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)
        return AnimeViewHolder(view)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture?.medium
        val animeTitle = animes[position].node.title
        val score = animes[position].node.mean
        val startTime = animes[position].node.broadcast?.start_time
        val weekDay = animes[position].node.broadcast?.day_of_the_week
        holder.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }
        holder.animeTitle.text = animeTitle
        val scoreValue = "${context.getString(R.string.score_value)} $score"
        holder.scoreText.text = scoreValue

        if (startTime!=null && weekDay!=null) {
            val jpTime = SeasonCalendar.getCurrentJapanHour()
            val startHour = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME).hour
            val currentWeekDay = SeasonCalendar.getCurrentJapanWeekday()
            val remaining = startHour - jpTime
            val h = context.getString(R.string.hour_abbreviation)
            val airingValue = if (currentWeekDay==weekDay && remaining > 0) {
                "${context.getString(R.string.airing_in)} ${remaining}$h"
            }
            else {
                "${context.getString(R.string.aired)} ${remaining.absoluteValue}$h ${context.getString(R.string.ago)}"
            }
            holder.airingText.text = airingValue
        } else {
            val airingValue = "${context.getString(R.string.airing_in)} ??"
            holder.airingText.text = airingValue
        }

        val anime = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
        }
        if (position == animes.size - 2) run {
            endListReachedListener?.onBottomReached(position)
        }
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}