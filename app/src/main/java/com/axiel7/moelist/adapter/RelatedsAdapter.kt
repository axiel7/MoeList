package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemAnimeRelatedBinding
import com.axiel7.moelist.model.Related
import com.axiel7.moelist.utils.StringFormat

class RelatedsAdapter(private val relateds: MutableList<Related>,
                      private val context: Context,
                      private val onClickListener: (View, Related) -> Unit) :
    RecyclerView.Adapter<RelatedsAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder(val binding: ListItemAnimeRelatedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemAnimeRelatedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = relateds[position].node.main_picture?.medium
        val animeTitle = relateds[position].node.title
        val relation = relateds[position].relation_type_formatted
        holder.binding.relatedPoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }
        holder.binding.animeTitle.text = animeTitle
        holder.binding.relationText.text = StringFormat.formatRelation(relation, context)

        val anime = relateds[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
        }
        if (position == relateds.size - 2) run {
            endListReachedListener?.onBottomReached(position, relateds.size)
        }
    }

    override fun getItemCount(): Int {
        return relateds.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}