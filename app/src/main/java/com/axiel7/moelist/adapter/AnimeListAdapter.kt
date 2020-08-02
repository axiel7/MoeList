package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.axiel7.moelist.R
import com.axiel7.moelist.model.AnimeList

class AnimeListAdapter(private val animes: List<AnimeList>, private val rowLayout: Int, private val context: Context) :
    RecyclerView.Adapter<AnimeListAdapter.AnimeViewHolder>() {
    private var mClickListener: ItemClickListener? = null

    inner class AnimeViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val animeLayout: LinearLayout = v.findViewById(R.id.anime_layout)
        val animeTitle: TextView = v.findViewById(R.id.anime_title)
        val animePoster: ImageView = v.findViewById(R.id.anime_poster)

        override fun onClick(view: View?) {
            mClickListener?.onItemClick(view, adapterPosition)
        }

        init {
            animePoster.clipToOutline = true
            v.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val posterUrl = animes[position].data.node.main_picture.medium
        val animeTitle = animes[position].data.node.title
        holder.animePoster.load(posterUrl)
        holder.animeTitle.text = animeTitle
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}