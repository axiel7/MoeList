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
import com.axiel7.moelist.model.MangaRanking
import com.axiel7.moelist.utils.StringFormat
import java.text.NumberFormat

class MangaRankingAdapter(private val mangas: MutableList<MangaRanking>,
                          private val rowLayout: Int,
                          private val context: Context,
                          private val onClickListener: (View, MangaRanking) -> Unit) :
    RecyclerView.Adapter<MangaRankingAdapter.AnimeViewHolder>() {
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
        val posterUrl = mangas[position].node.main_picture.medium
        val animeTitle = mangas[position].node.title
        val ranking = mangas[position].ranking?.rank
        val mediaType = mangas[position].node.media_type?.let { StringFormat.formatMediaType(it, context) }
        val episodes = mangas[position].node.num_chapters
        val score = mangas[position].node.mean
        val members = NumberFormat.getInstance().format(mangas[position].node.num_list_users)
        holder.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }

        holder.animeTitle.text = animeTitle

        val rankValue = "#$ranking"
        holder.rankingText.text = rankValue

        val mediaStatusValue = if (episodes==0) { "$mediaType (?? ${context.getString(R.string.chapters)})" }
        else { "$mediaType ($episodes ${context.getString(R.string.chapters)})" }
        holder.mediaStatusText.text = mediaStatusValue

        val scoreValue = "${context.getString(R.string.score_value)} $score"
        holder.scoreText.text = scoreValue

        holder.membersText.text = members

        val anime = mangas[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
        }
        if (position == mangas.size - 2) run {
            endListReachedListener?.onBottomReached(position)
        }
    }

    override fun getItemCount(): Int {
        return mangas.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}