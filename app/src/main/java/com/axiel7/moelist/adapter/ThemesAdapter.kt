package com.axiel7.moelist.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.databinding.ListItemThemeBinding
import com.axiel7.moelist.model.Theme

class ThemesAdapter(private val themes: MutableList<Theme>,
                    private val context: Context) :
    RecyclerView.Adapter<ThemesAdapter.AnimeViewHolder>() {
    private var endListReachedListener: EndListReachedListener? = null

    class AnimeViewHolder(val binding: ListItemThemeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ListItemThemeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val themeText = themes[position].text

        holder.binding.theme.text = themeText

        var query = themeText.replace(" ", "+", true)
        if (query.startsWith("#")) {
            query = query.replaceFirst("#", "")
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.youtube.com/results?search_query=$query")
            context.startActivity(intent)
        }
        holder.itemView.setOnLongClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("theme", themeText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
            true
        }
        if (position == themes.size - 2) run {
            endListReachedListener?.onBottomReached(position, themes.size)
        }
    }

    override fun getItemCount(): Int {
        return themes.size
    }

    fun setEndListReachedListener(endListReachedListener: EndListReachedListener?) {
        this.endListReachedListener = endListReachedListener
    }
}