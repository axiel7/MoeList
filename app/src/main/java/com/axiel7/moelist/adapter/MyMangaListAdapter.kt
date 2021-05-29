package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ListItemMangalistBinding
import com.axiel7.moelist.model.UserMangaList
import com.axiel7.moelist.utils.StringFormat

class MyMangaListAdapter(private val mangas: MutableList<UserMangaList>,
                         private val context: Context,
                         private val onClickListener: (View, UserMangaList, Int) -> Unit,
                         private val onLongClickListener: (View, UserMangaList, Int) -> Unit) :
    RecyclerView.Adapter<MyMangaListAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null
    private var plusButtonTouchedListener: PlusButtonTouchedListener? = null

    inner class AnimeViewHolder(val binding: ListItemMangalistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemMangalistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = mangas[position].node.main_picture?.medium
        val mangaTitle = mangas[position].node.title
        val mangaScore = mangas[position].list_status?.score
        val chaptersRead = mangas[position].list_status?.num_chapters_read ?: 0
        val totalChapters = mangas[position].node.num_chapters ?: 0
        var mediaType = mangas[position].node.media_type
        var status = mangas[position].node.status
        status = status?.let { StringFormat.formatStatus(it, context) }
        mediaType = mediaType?.let { StringFormat.formatMediaType(it, context) }

        val progressText = "$chaptersRead/$totalChapters"
        val mediaStatus = "$mediaType • $status"

        holder.binding.mangaPoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }
        holder.binding.mangaTitle.text = mangaTitle

        if (mangaScore==0) {
            holder.binding.scoreText.text = "─"
        } else {
            holder.binding.scoreText.text = mangaScore.toString()
        }

        holder.binding.progressText.text = progressText
        holder.binding.mediaStatus.text = mediaStatus
        when {
            totalChapters != 0 || chaptersRead == 0 -> {
                holder.binding.chaptersProgress.max = totalChapters
                holder.binding.chaptersProgress.progress = chaptersRead
            }
            else -> {
                holder.binding.chaptersProgress.max = 100
                holder.binding.chaptersProgress.progress = 50
            }
        }

        if (position == mangas.size - 3) run {
            endListReachedListener?.onBottomReached(position, mangas.size)
        }

        val manga = mangas[position]
        holder.itemView.setOnClickListener { view ->
            onClickListener(view, manga, position)
        }
        holder.itemView.setOnLongClickListener { view ->
            onLongClickListener(view, manga, position)
            true
        }
        val listStatus = mangas[position].list_status
        if (listStatus?.status == "reading" || listStatus?.is_rereading == true) {
            holder.binding.addOneButton.visibility = View.VISIBLE
            holder.binding.addOneButton.isEnabled = true
        }
        else {
            holder.binding.addOneButton.visibility = View.INVISIBLE
            holder.binding.addOneButton.isEnabled = false
        }
    }

    override fun getItemCount(): Int {
        return mangas.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }

    fun setPlusButtonTouchedListener(plusButtonTouchedListener: PlusButtonTouchedListener?) {
        this.plusButtonTouchedListener = plusButtonTouchedListener
    }

    fun getAnimeId(position: Int): Int {
        return mangas[position].node.id
    }
    fun getWatchedEpisodes(position: Int): Int? {
        return mangas[position].list_status?.num_chapters_read
    }
    fun getTotalEpisodes(position: Int): Int? {
        return mangas[position].node.num_chapters
    }
}