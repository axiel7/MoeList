package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.axiel7.moelist.R
import com.axiel7.moelist.model.SeasonalList
import com.axiel7.moelist.utils.StringFormat

class SeasonalAnimeAdapter(private val animes: MutableList<SeasonalList>,
                           private val rowLayout: Int,
                           private val context: Context,
                           private val onClickListener: (View, SeasonalList) -> Unit) :
    RecyclerView.Adapter<SeasonalAnimeAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val animeTitle: TextView = v.findViewById(R.id.anime_title)
        val animePoster: ImageView = v.findViewById(R.id.anime_poster)
        val mediaStatusView: TextView = v.findViewById(R.id.media_status)
        val scoreView: TextView = v.findViewById(R.id.score_text)
        val broadcastView: TextView = v.findViewById(R.id.broadcast_text)

        init {
            animePoster.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)
        return AnimeViewHolder(view)
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

        holder.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.animeTitle.text = mangaTitle

        val mediaStatus = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.episodes)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.episodes)})" }
        holder.mediaStatusView.text = mediaStatus

        val scoreText = score?.toString() ?: "??"
        holder.scoreView.text = scoreText

        val broadcastValue = "$weekDay $startTime"
        holder.broadcastView.text = if (broadcast!=null) { broadcastValue }
        else { context.getString(R.string.unknown) }

        val manga = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, manga)
        }
        if (position == animes.size - 2) run {
            endListReachedListener?.onBottomReached(position)
        }
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}