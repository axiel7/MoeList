package com.axiel7.moelist.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.axiel7.moelist.adapter.base.BaseAdapter
import com.axiel7.moelist.data.model.anime.Theme
import com.axiel7.moelist.databinding.ListItemThemeBinding
import com.axiel7.moelist.utils.UseCases.copyToClipBoard

class ThemesAdapter(
    private val context: Context
) : BaseAdapter<ListItemThemeBinding, Theme>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemThemeBinding
        get() = ListItemThemeBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: Theme) {
        val themeText = item.text

        holder.binding.theme.text = themeText

        val query = getQueryFromThemeText(themeText)
    
        holder.itemView.setOnClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.youtube.com/results?search_query=$query")
                context.startActivity(this)
            }
        }

        holder.itemView.setOnLongClickListener {
            themeText.copyToClipBoard(context)
            true
        }
    }

    private fun getQueryFromThemeText(themeText: String): String {
        var query = themeText.replace(" ", "+")
        val size = query.length

        if (query.startsWith("#")) {
            query = query.substring(4, size)
        }
        val index = query.indexOf("(ep")

        return if (index == -1) query
        else query.substring(0, index - 1)
    }

}