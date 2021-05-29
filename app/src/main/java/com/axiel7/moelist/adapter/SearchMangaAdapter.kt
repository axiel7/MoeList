package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemSearchResultBinding
import com.axiel7.moelist.model.MangaList
import com.axiel7.moelist.utils.StringFormat

class SearchMangaAdapter(private val animes: MutableList<MangaList>,
                         private val context: Context,
                         private val onClickListener: (View, MangaList) -> Unit) :
    RecyclerView.Adapter<SearchMangaAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder(val binding: ListItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture?.medium
        val animeTitle = animes[position].node.title
        val mediaType = animes[position].node.media_type?.let { StringFormat.formatMediaType(it, context) }
        val episodes = animes[position].node.num_chapters
        val startDate = animes[position].node.start_date
        val score = animes[position].node.mean

        holder.binding.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.binding.animeTitle.text = animeTitle

        val mediaStatus = "$mediaType ($episodes ${context.getString(R.string.chapters)})"
        holder.binding.mediaStatus.text = mediaStatus

        holder.binding.yearText.text = startDate

        val scoreText: String = if (score == null) {
            "${context.getString(R.string.score_value)} ??"
        } else {
            "${context.getString(R.string.score_value)} $score"
        }
        holder.binding.scoreText.text = scoreText

        val anime = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
        }
        if (position == animes.size - 2) run {
            endListReachedListener?.onBottomReached(position, animes.size)
        }
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}