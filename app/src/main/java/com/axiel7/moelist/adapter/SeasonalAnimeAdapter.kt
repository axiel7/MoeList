package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemSeasonalBinding
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.utils.StringFormat

class SeasonalAnimeAdapter(private val animes: MutableList<SeasonalList>,
                           private val context: Context,
                           private val onClickListener: (View, SeasonalList) -> Unit) :
    RecyclerView.Adapter<SeasonalAnimeAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder(val binding: ListItemSeasonalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemSeasonalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture?.medium
        val mangaTitle = animes[position].node.title
        val mediaType = animes[position].node.media_type?.let { StringFormat.formatMediaType(it, context) }
        val episodes = animes[position].node.num_episodes
        val broadcast = animes[position].node.broadcast
        val weekDay = StringFormat.formatWeekday(broadcast?.day_of_the_week, context)
        val startTime = broadcast?.start_time
        val score = animes[position].node.mean

        holder.binding.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.binding.animeTitle.text = mangaTitle

        val mediaStatus = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.episodes)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.episodes)})" }
        holder.binding.mediaStatus.text = mediaStatus

        val scoreText = score?.toString() ?: "??"
        holder.binding.scoreText.text = scoreText

        val broadcastValue = "$weekDay $startTime"
        holder.binding.broadcastText.text = if (broadcast!=null) { broadcastValue }
        else { context.getString(R.string.unknown) }

        val manga = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, manga)
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