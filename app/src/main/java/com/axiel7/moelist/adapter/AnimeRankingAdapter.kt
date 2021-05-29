package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemRankingBinding
import com.axiel7.moelist.model.AnimeRanking
import com.axiel7.moelist.utils.StringFormat
import java.text.NumberFormat

class AnimeRankingAdapter(private val animes: MutableList<AnimeRanking>,
                          private val context: Context,
                          private val onClickListener: (View, AnimeRanking) -> Unit) :
    RecyclerView.Adapter<AnimeRankingAdapter.AnimeViewHolder>() {
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
        val posterUrl = animes[position].node.main_picture?.medium
        val animeTitle = animes[position].node.title
        val ranking = animes[position].ranking?.rank
        val mediaType = animes[position].node.media_type?.let { StringFormat.formatMediaType(it, context) }
        val episodes = animes[position].node.num_episodes
        val score = animes[position].node.mean
        val members = NumberFormat.getInstance().format(animes[position].node.num_list_users)
        holder.binding.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.binding.animeTitle.text = animeTitle

        val rankValue = "#$ranking"
        holder.binding.rankingText.text = rankValue

        val mediaStatusValue = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.episodes)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.episodes)})" }
        holder.binding.mediaStatus.text = mediaStatusValue

        holder.binding.scoreText.text = score.toString()

        holder.binding.membersText.text = members

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