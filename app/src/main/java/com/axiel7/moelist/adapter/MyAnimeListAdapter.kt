package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemAnimelistBinding
import com.axiel7.moelist.model.UserAnimeList
import com.axiel7.moelist.utils.StringFormat

class MyAnimeListAdapter(private val animes: MutableList<UserAnimeList>,
                         private val context: Context,
                         private val onClickListener: (View, UserAnimeList, Int) -> Unit,
                         private val onLongClickListener: (View, UserAnimeList, Int) -> Unit) :
    RecyclerView.Adapter<MyAnimeListAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null
    private var plusButtonTouchedListener: PlusButtonTouchedListener? = null

    inner class AnimeViewHolder(val binding: ListItemAnimelistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemAnimelistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].node.main_picture?.medium
        val animeTitle = animes[position].node.title
        val animeScore = animes[position].list_status?.score
        val watchedEpisodes = animes[position].list_status?.num_episodes_watched ?: 0
        val totalEpisodes = animes[position].node.num_episodes ?: 0
        var mediaType = animes[position].node.media_type
        var status = animes[position].node.status
        status = status?.let { StringFormat.formatStatus(it, context) }
        mediaType = mediaType?.let { StringFormat.formatMediaType(it, context) }

        val progressText = "$watchedEpisodes/$totalEpisodes"
        val mediaStatus = "$mediaType • $status"

        holder.binding.animePoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }
        holder.binding.animeTitle.text = animeTitle

        if (animeScore==0) {
            holder.binding.scoreText.text = "─"
        } else {
            holder.binding.scoreText.text = animeScore.toString()
        }

        holder.binding.progressText.text = progressText
        holder.binding.mediaStatus.text = mediaStatus
        when {
            totalEpisodes != 0 || watchedEpisodes == 0 -> {
                holder.binding.episodesProgress.max = totalEpisodes
                holder.binding.episodesProgress.progress = watchedEpisodes
            }
            else -> {
                holder.binding.episodesProgress.max = 100
                holder.binding.episodesProgress.progress = 50
            }
        }

        if (position == animes.size - 3) run {
            endListReachedListener?.onBottomReached(position, animes.size)
        }

        val anime = animes[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, anime, position)
        }
        holder.itemView.setOnLongClickListener { view ->
            onLongClickListener(view, anime, position)
            true
        }
        val listStatus = animes[position].list_status
        if (listStatus?.status == "watching" || listStatus?.is_rewatching == true) {
            holder.binding.addOneButton.visibility = View.VISIBLE
            holder.binding.addOneButton.isEnabled = true
        }
        else {
            holder.binding.addOneButton.visibility = View.INVISIBLE
            holder.binding.addOneButton.isEnabled = false
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