package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.base.BasePagingAdapter
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.databinding.ListItemSearchResultBinding
import com.axiel7.moelist.utils.StringExtensions.formatMediaType

class SearchAnimeAdapter(
    private val context: Context,
    private val onClick: (View, AnimeList, Int) -> Unit,
) : BasePagingAdapter<ListItemSearchResultBinding, AnimeList>(Comparator) {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemSearchResultBinding
        get() = ListItemSearchResultBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: AnimeList) {

        holder.binding.poster.load(item.node.mainPicture?.medium)

        holder.binding.title.text = item.node.title

        val mediaType = item.node.mediaType?.formatMediaType(context)
        val episodes = item.node.numEpisodes
        holder.binding.mediaStatus.text = if (episodes == 0) "$mediaType (?? ${context.getString(R.string.episodes)})"
        else "$mediaType ($episodes ${context.getString(R.string.episodes)})"

        holder.binding.year.text = item.node.startSeason?.year?.toString() ?: context.getString(R.string.unknown)

        holder.binding.score.text = item.node.mean?.toString() ?: "??"

        holder.itemView.setOnClickListener { onClick(it, item, position) }
    }

    object Comparator : DiffUtil.ItemCallback<AnimeList>() {
        override fun areItemsTheSame(oldItem: AnimeList, newItem: AnimeList): Boolean {
            return oldItem.node.id == newItem.node.id
        }

        override fun areContentsTheSame(oldItem: AnimeList, newItem: AnimeList): Boolean {
            return oldItem == newItem
        }
    }
}