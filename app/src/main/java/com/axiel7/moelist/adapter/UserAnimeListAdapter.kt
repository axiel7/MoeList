package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.axiel7.moelist.adapter.base.BasePagingAdapter
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.databinding.ListItemAnimelistBinding
import com.axiel7.moelist.utils.StringExtensions.formatMediaType
import com.axiel7.moelist.utils.StringExtensions.formatStatus

class UserAnimeListAdapter(
    private val context: Context,
    private val onClick: (View, UserAnimeList, Int) -> Unit,
    private val onLongClick: (View, UserAnimeList, Int) -> Unit,
    private val onPlusButtonClick: (View, UserAnimeList, Int) -> Unit
) : BasePagingAdapter<ListItemAnimelistBinding, UserAnimeList>(Comparator) {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemAnimelistBinding
            = ListItemAnimelistBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: UserAnimeList) {

        holder.binding.poster.load(item.node.mainPicture?.medium)

        holder.binding.title.text = item.node.title

        val score = item.listStatus?.score
        holder.binding.score.text = if (score == 0) "─" else score.toString()

        val watchedEpisodes = item.listStatus?.numEpisodesWatched ?: 0
        val totalEpisodes = item.node.numEpisodes ?: 0
        holder.binding.progressText.text = "$watchedEpisodes/$totalEpisodes"

        val mediaType = item.node.mediaType?.formatMediaType(context)
        val status = item.node.status?.formatStatus(context)
        holder.binding.mediaStatus.text = "$mediaType • $status"

        when {
            totalEpisodes != 0 || watchedEpisodes == 0 -> {
                holder.binding.episodesProgress.max = totalEpisodes
                holder.binding.episodesProgress.progress = watchedEpisodes
            }
            else -> {
                holder.binding.episodesProgress.max = 100
                holder.binding.episodesProgress.progress = 50
            }
        }

        holder.itemView.setOnClickListener { onClick(it, item, position) }
        holder.itemView.setOnLongClickListener {
            onLongClick(it, item, position)
            true
        }
        holder.binding.addOneButton.setOnClickListener { onPlusButtonClick(it, item, position) }

        if (item.listStatus?.status == "watching" || item.listStatus?.isRewatching == true) {
            holder.binding.addOneButton.visibility = View.VISIBLE
            holder.binding.addOneButton.isEnabled = true
        }
        else {
            holder.binding.addOneButton.visibility = View.INVISIBLE
            holder.binding.addOneButton.isEnabled = false
        }
    }

    object Comparator : DiffUtil.ItemCallback<UserAnimeList>() {
        override fun areItemsTheSame(oldItem: UserAnimeList, newItem: UserAnimeList): Boolean {
            return oldItem.node.id == newItem.node.id
        }

        override fun areContentsTheSame(oldItem: UserAnimeList, newItem: UserAnimeList): Boolean {
            return oldItem == newItem
        }
    }
}