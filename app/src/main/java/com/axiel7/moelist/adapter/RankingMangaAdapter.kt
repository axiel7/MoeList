package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.base.BasePagingAdapter
import com.axiel7.moelist.data.model.manga.MangaRanking
import com.axiel7.moelist.databinding.ListItemRankingBinding
import com.axiel7.moelist.utils.StringExtensions.formatMediaType
import java.text.NumberFormat

class RankingMangaAdapter(
    private val context: Context,
    private val onClick: (View, MangaRanking) -> Unit
) : BasePagingAdapter<ListItemRankingBinding, MangaRanking>(Comparator) {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemRankingBinding
        get() = ListItemRankingBinding::inflate
    private val numberFormat = NumberFormat.getInstance()

    override fun loadData(holder: ViewHolder, position: Int, item: MangaRanking) {

        holder.binding.poster.load(item.node.mainPicture?.medium)

        holder.binding.title.text = item.node.title

        holder.binding.ranking.text = "#${item.ranking?.rank}"

        val mediaType = item.node.mediaType?.formatMediaType(context)
        val chapters = item.node.numChapters
        val mediaStatus = if (chapters == 0) "$mediaType (?? ${context.getString(R.string.chapters)})"
        else "$mediaType ($chapters ${context.getString(R.string.chapters)})"
        holder.binding.mediaStatus.text = mediaStatus

        holder.binding.score.text = item.node.mean.toString()

        holder.binding.members.text = numberFormat.format(item.node.numListUsers ?: 0)

        holder.itemView.setOnClickListener { onClick(it, item) }
    }

    object Comparator : DiffUtil.ItemCallback<MangaRanking>() {
        override fun areItemsTheSame(oldItem: MangaRanking, newItem: MangaRanking): Boolean {
            return oldItem.node.id == newItem.node.id
        }

        override fun areContentsTheSame(oldItem: MangaRanking, newItem: MangaRanking): Boolean {
            return oldItem == newItem
        }
    }

}