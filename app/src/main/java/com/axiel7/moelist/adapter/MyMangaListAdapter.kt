package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.model.UserMangaList
import com.axiel7.moelist.utils.StringFormat

class MyMangaListAdapter(private val mangas: MutableList<UserMangaList>,
                         private val rowLayout: Int,
                         private val context: Context,
                         private val onClickListener: (View, UserMangaList, Int) -> Unit,
                         private val onLongClickListener: (View, UserMangaList, Int) -> Unit) :
    RecyclerView.Adapter<MyMangaListAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null
    private var plusButtonTouchedListener: PlusButtonTouchedListener? = null

    inner class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val mangaTitle: TextView = v.findViewById(R.id.manga_title)
        val mangaPoster: ImageView = v.findViewById(R.id.manga_poster)
        val mangaScore: TextView = v.findViewById(R.id.score_text)
        val progressText: TextView = v.findViewById(R.id.progress_text)
        val progressBar: ProgressBar = v.findViewById(R.id.chapters_progress)
        val mediaStatus: TextView = v.findViewById(R.id.media_status)
        val plusButton: Button = v.findViewById(R.id.add_one_button)

        init {
            plusButton.setOnClickListener { view ->
                plusButtonTouchedListener?.onButtonTouched(view, adapterPosition) }
            mangaPoster.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)

        return AnimeViewHolder(view)
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

        holder.mangaPoster.load(posterUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_launcher_foreground)
            allowHardware(false)
        }
        holder.mangaTitle.text = mangaTitle

        if (mangaScore==0) {
            holder.mangaScore.text = "─"
        } else {
            holder.mangaScore.text = mangaScore.toString()
        }

        holder.progressText.text = progressText
        holder.mediaStatus.text = mediaStatus
        when {
            totalChapters != 0 || chaptersRead == 0 -> {
                holder.progressBar.max = totalChapters
                holder.progressBar.progress = chaptersRead
            }
            else -> {
                holder.progressBar.max = 100
                holder.progressBar.progress = 50
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
            holder.plusButton.visibility = View.VISIBLE
            holder.plusButton.isEnabled = true
        }
        else {
            holder.plusButton.visibility = View.INVISIBLE
            holder.plusButton.isEnabled = false
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