package com.axiel7.moelist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.axiel7.moelist.adapter.base.BasePagingAdapter
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.databinding.ListItemAnimeBinding

class CurrentSeasonalAdapter(
    private val onClick: (View, AnimeSeasonal) -> Unit
) : BasePagingAdapter<ListItemAnimeBinding, AnimeSeasonal>(Comparator) {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemAnimeBinding
        get() = ListItemAnimeBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: AnimeSeasonal) {

        holder.binding.poster.load(item.node.mainPicture?.medium)

        holder.binding.title.text = item.node.title

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