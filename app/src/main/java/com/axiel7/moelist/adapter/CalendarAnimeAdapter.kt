package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.base.BaseAdapter
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.databinding.ListItemSeasonalBinding
import com.axiel7.moelist.utils.StringExtensions.formatMediaType

class CalendarAnimeAdapter(
    private val context: Context,
    private val onClick: (View, AnimeSeasonal) -> Unit
) : BaseAdapter<ListItemSeasonalBinding, AnimeSeasonal>() {

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
            holder.binding.broadcast.text = item.node.broadcast.startTime ?: context.getString(R.string.unknown)
        } else {
            holder.binding.broadcast.text = context.getString(R.string.unknown)
        }

        holder.itemView.setOnClickListener { onClick(it, item) }

    }

}