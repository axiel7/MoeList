package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.base.BasePagingAdapter
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.databinding.ListItemSeasonalBinding
import com.axiel7.moelist.utils.StringExtensions.formatMediaType
import com.axiel7.moelist.utils.StringExtensions.formatWeekday

class SeasonalAnimeAdapter(
    private val context: Context,
    private val onClick: (View, AnimeSeasonal) -> Unit
) : BasePagingAdapter<ListItemSeasonalBinding, AnimeSeasonal>(Comparator) {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemSeasonalBinding
        get() = ListItemSeasonalBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: AnimeSeasonal) {
        holder.binding.poster.load(item.node.mainPicture?.medium)

        holder.binding.title.text = item.node.title

        val episodes = item.node.numEpisodes
        val mediaType = item.node.mediaType?.formatMediaType(context)
        val mediaStatus = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.episodes)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.episodes)})" }
        holder.binding.mediaStatus.text = mediaStatus

        holder.binding.score.text = item.node.mean?.toString() ?: "??"

        if (item.node.broadcast != null) {
            val weekDay = item.node.broadcast.dayOfTheWeek.formatWeekday(context)
            holder.binding.broadcast.text = "$weekDay ${item.node.broadcast.startTime}"
        } else {
            holder.binding.broadcast.text = context.getString(R.string.unknown)
        }

        holder.itemView.setOnClickListener { onClick(it, item) }

    }

    object Comparator : DiffUtil.ItemCallback<AnimeSeasonal>() {
        override fun areItemsTheSame(oldItem: AnimeSeasonal, newItem: AnimeSeasonal): Boolean {
            return oldItem.node.id == newItem.node.id
        }

        override fun areContentsTheSame(oldItem: AnimeSeasonal, newItem: AnimeSeasonal): Boolean {
            return oldItem == newItem
        }
    }
}