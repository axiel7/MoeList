package com.axiel7.moelist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.axiel7.moelist.R
import com.axiel7.moelist.model.UserAnimeList
import com.axiel7.moelist.utils.StringFormat

class MyAnimeListAdapter(private val animes: MutableList<UserAnimeList>,
                         private val rowLayout: Int,
                         private val onClickListener: (View, UserAnimeList) -> Unit) :
    RecyclerView.Adapter<MyAnimeListAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null
    private var plusButtonTouchedListener: PlusButtonTouchedListener? = null

    inner class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val animeTitle: TextView = v.findViewById(R.id.anime_title)
        val animePoster: ImageView = v.findViewById(R.id.anime_poster)
        val animeScore: TextView = v.findViewById(R.id.score_text)
        val progressText: TextView = v.findViewById(R.id.progress_text)
        val progressBar: ProgressBar = v.findViewById(R.id.episodes_progress)
        val mediaStatus: TextView = v.findViewById(R.id.media_status)
        val plusButton: Button = v.findViewById(R.id.add_one_button)

        init {
            plusButton.setOnClickListener { view ->
                plusButtonTouchedListener?.onButtonTouched(view, adapterPosition) }
            animePoster.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)

        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture.medium
        val animeTitle = animes[position].node.title
        val animeScore = animes[position].list_status?.score
        val watchedEpisodes = animes[position].list_status?.num_episodes_watched
        val totalEpisodes = animes[position].node.num_episodes
        var mediaType = animes[position].node.media_type
        var status = animes[position].node.status
        status = status?.let { StringFormat.formatStatus(it) }
        mediaType = mediaType?.let { StringFormat.formatMediaType(it) }

        val progressText = "$watchedEpisodes/$totalEpisodes"
        val mediaStatus = "$mediaType • $status"

        holder.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }
        holder.animeTitle.text = animeTitle
        holder.animeScore.text = animeScore.toString()
        holder.progressText.text = progressText
        holder.mediaStatus.text = mediaStatus
        if (totalEpisodes != null && watchedEpisodes != null) {
            holder.progressBar.max = totalEpisodes
            holder.progressBar.progress = watchedEpisodes
        }
        else {
            holder.progressBar.max = 100
            holder.progressBar.progress = 50
        }

        if (position == animes.size - 2) run {
            endListReachedListener?.onBottomReached(position)
        }

        val anime = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime)
        }
        if (animes[position].list_status?.status.equals("watching")) {
            holder.plusButton.visibility = View.VISIBLE
            holder.plusButton.isEnabled = true
        }
        else {
            holder.plusButton.visibility = View.INVISIBLE
            holder.plusButton.isEnabled = false
        }
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }

    fun setPlusButtonTouchedListener(plusButtonTouchedListener: PlusButtonTouchedListener?) {
        this.plusButtonTouchedListener = plusButtonTouchedListener
    }

    fun getAnimeId(position: Int): Int {
        return animes[position].node.id
    }
    fun getWatchedEpisodes(position: Int): Int? {
        return animes[position].list_status?.num_episodes_watched
    }
    fun getTotalEpisodes(position: Int): Int? {
        return animes[position].node.num_episodes
    }
}