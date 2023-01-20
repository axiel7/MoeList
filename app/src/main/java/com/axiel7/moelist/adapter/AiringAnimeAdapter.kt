package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.base.BaseAdapter
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.databinding.ListItemAnimeTodayBinding
import com.axiel7.moelist.utils.Extensions.toStringOrNull
import com.axiel7.moelist.utils.SeasonCalendar
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

class AiringAnimeAdapter(
    private val context: Context,
    private val onClick: (View, AnimeSeasonal) -> Unit
) : BaseAdapter<ListItemAnimeTodayBinding, AnimeSeasonal>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemAnimeTodayBinding
        get() = ListItemAnimeTodayBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: AnimeSeasonal) {
        holder.binding.poster.load(item.node.mainPicture?.medium)
        holder.binding.title.text = item.node.title
        val score = item.node.mean.toStringOrNull() ?: context.getString(R.string.unknown)
        holder.binding.score.text = "${context.getString(R.string.score_value)} $score"

        val startTime = item.node.broadcast?.startTime
        val weekDay = item.node.broadcast?.dayOfTheWeek

        if (startTime != null && weekDay != null) {
            val jpTime = SeasonCalendar.currentJapanHour
            val startHour = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME).hour
            val currentJpWeekDay = SeasonCalendar.currentJapanWeekday
            val remaining = startHour - jpTime
            val airingValue = if (currentJpWeekDay == weekDay && remaining > 0)
                context.getString(R.string.airing_in).format(remaining)
            else context.getString(R.string.aired_ago).format(remaining.absoluteValue)

            holder.binding.airingTime.text = airingValue
        } else {
            holder.binding.airingTime.text = "${context.getString(R.string.airing_in)} ??"
        }

        holder.itemView.setOnClickListener { onClick(it, item) }
    }

}