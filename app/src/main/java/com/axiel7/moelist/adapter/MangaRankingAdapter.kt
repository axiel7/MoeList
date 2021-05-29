package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemRankingBinding
import com.axiel7.moelist.model.MangaRanking
import com.axiel7.moelist.utils.StringFormat
import java.text.NumberFormat

class MangaRankingAdapter(private val mangas: MutableList<MangaRanking>,
                          private val context: Context,
                          private val onClickListener: (View, MangaRanking) -> Unit) :
    RecyclerView.Adapter<MangaRankingAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder(val binding: ListItemRankingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemRankingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = mangas[position].node.main_picture?.medium
        val animeTitle = mangas[position].node.title
        val ranking = mangas[position].ranking?.rank
        val mediaType = mangas[position].node.media_type?.let { StringFormat.formatMediaType(it, context) }
        val episodes = mangas[position].node.num_chapters
        val score = mangas[position].node.mean
        val members = NumberFormat.getInstance().format(mangas[position].node.num_list_users)
        holder.binding.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.binding.animeTitle.text = animeTitle

        val rankValue = "#$ranking"
        holder.binding.rankingText.text = rankValue

        val mediaStatusValue = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.chapters)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.chapters)})" }
        holder.binding.mediaStatus.text = mediaStatusValue

        val scoreValue = "${context.getString(R.string.score_value)} $score"
        holder.binding.scoreText.text = scoreValue

        holder.binding.membersText.text = members

        val anime = mangas[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
        }
        if (position == mangas.size - 2) run {
            endListReachedListener?.onBottomReached(position, mangas.size)
        }
    }

    override fun getItemCount(): Int {
        return mangas.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}