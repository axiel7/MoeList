package com.axiel7.moelist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.axiel7.moelist.R
import com.axiel7.moelist.model.MangaList
import com.axiel7.moelist.utils.StringFormat

class SearchMangaAdapter(private val animes: MutableList<MangaList>,
                         private val rowLayout: Int,
                         private val onClickListener: (View, MangaList) -> Unit) :
    RecyclerView.Adapter<SearchMangaAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    inner class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val animeTitleView: TextView = v.findViewById(R.id.anime_title)
        val animePosterView: ImageView = v.findViewById(R.id.anime_poster)
        val mediaStatusView: TextView = v.findViewById(R.id.media_status)
        val yearView: TextView = v.findViewById(R.id.year_text)
        val scoreView: TextView = v.findViewById(R.id.score_text)

        init {
            animePosterView.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture.medium
        val animeTitle = animes[position].node.title
        val mediaType = animes[position].node.media_type?.let { StringFormat.formatMediaType(it) }
        val episodes = animes[position].node.num_chapters
        val startDate = animes[position].node.start_date
        val score = animes[position].node.mean

        holder.animePosterView.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.animeTitleView.text = animeTitle

        val mediaStatus = "$mediaType ($episodes Chapters)"
        holder.mediaStatusView.text = mediaStatus

        holder.yearView.text = startDate

        val scoreText: String
        scoreText = if (score == null) {
            "Score: ??"
        } else {
            "Score: $score"
        }
        holder.scoreView.text = scoreText

        val anime = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
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