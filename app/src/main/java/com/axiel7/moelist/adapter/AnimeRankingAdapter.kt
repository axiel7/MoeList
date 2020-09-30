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
import com.axiel7.moelist.model.AnimeRanking
import com.axiel7.moelist.utils.StringFormat
import java.text.NumberFormat

class AnimeRankingAdapter(private val animes: MutableList<AnimeRanking>,
                          private val rowLayout: Int,
                          private val context: Context,
                          private val onClickListener: (View, AnimeRanking) -> Unit) :
    RecyclerView.Adapter<AnimeRankingAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val animeTitle: TextView = v.findViewById(R.id.anime_title)
        val animePoster: ImageView = v.findViewById(R.id.anime_poster)
        val rankingText: TextView = v.findViewById(R.id.ranking_text)
        val mediaStatusText: TextView = v.findViewById(R.id.media_status)
        val scoreText: TextView = v.findViewById(R.id.score_text)
        val membersText: TextView = v.findViewById(R.id.members_text)

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
        val animeTitle = animes[position].node.title
        val ranking = animes[position].ranking?.rank
        val mediaType = animes[position].node.media_type?.let { StringFormat.formatMediaType(it, context) }
        val episodes = animes[position].node.num_episodes
        val score = animes[position].node.mean
        val members = NumberFormat.getInstance().format(animes[position].node.num_list_users)
        holder.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.animeTitle.text = animeTitle

        val rankValue = "#$ranking"
        holder.rankingText.text = rankValue

        val mediaStatusValue = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.episodes)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.episodes)})" }
        holder.mediaStatusText.text = mediaStatusValue

        holder.scoreText.text = score.toString()

        holder.membersText.text = members

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