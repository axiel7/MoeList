package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.axiel7.moelist.adapter.base.BaseAdapter
import com.axiel7.moelist.data.model.media.Related
import com.axiel7.moelist.databinding.ListItemAnimeRelatedBinding
import com.axiel7.moelist.utils.StringExtensions.formatRelation

class RelatedsAdapter(
    private val context: Context,
    private val onClick: (View, Related) -> Unit
) : BaseAdapter<ListItemAnimeRelatedBinding, Related>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemAnimeRelatedBinding
        get() = ListItemAnimeRelatedBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: Related) {
        holder.binding.poster.load(item.node.mainPicture?.medium)

        holder.binding.title.text = item.node.title

        holder.binding.relation.text = item.relationTypeFormatted.formatRelation(context)

        holder.itemView.setOnClickListener { onClick(it, item) }
    }
}