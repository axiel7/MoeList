package com.axiel7.moelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.axiel7.moelist.adapter.base.BasePagingAdapter
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.databinding.ListItemMangalistBinding
import com.axiel7.moelist.utils.StringExtensions.formatMediaType
import com.axiel7.moelist.utils.StringExtensions.formatStatus

class UserMangaListAdapter(
    private val context: Context,
    private val onClick: (View, UserMangaList, Int) -> Unit,
    private val onLongClick: (View, UserMangaList, Int) -> Unit,
    private val onPlusButtonClick: (View, UserMangaList, Int) -> Unit
) : BasePagingAdapter<ListItemMangalistBinding, UserMangaList>(Comparator) {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListItemMangalistBinding
            = ListItemMangalistBinding::inflate

    override fun loadData(holder: ViewHolder, position: Int, item: UserMangaList) {

        holder.binding.poster.load(item.node.mainPicture?.medium)

        holder.binding.title.text = item.node.title

        val score = item.listStatus?.score
        holder.binding.score.text = if (score == 0) "─" else score.toString()

        val chaptersRead = item.listStatus?.numChaptersRead ?: 0
        val totalChapters = item.node.numChapters ?: 0
        holder.binding.progressText.text = "$chaptersRead/$totalChapters"

        val mediaType = item.node.mediaType?.formatMediaType(context)
        val status = item.node.status?.formatStatus(context)
        holder.binding.mediaStatus.text = "$mediaType • $status"

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

        holder.itemView.setOnClickListener { onClick(it, item, position) }
        holder.itemView.setOnLongClickListener {
            onLongClick(it, item, position)
            true
        }
        holder.binding.addOneButton.setOnClickListener { onPlusButtonClick(it, item, position) }

        if (item.listStatus?.status == "reading" || item.listStatus?.isRereading == true) {
            holder.binding.addOneButton.visibility = View.VISIBLE
            holder.binding.addOneButton.isEnabled = true
        }
        else {
            holder.binding.addOneButton.visibility = View.INVISIBLE
            holder.binding.addOneButton.isEnabled = false
        }
    }

    object Comparator : DiffUtil.ItemCallback<UserMangaList>() {
        override fun areItemsTheSame(oldItem: UserMangaList, newItem: UserMangaList): Boolean {
            return oldItem.node.id == newItem.node.id
        }

        override fun areContentsTheSame(oldItem: UserMangaList, newItem: UserMangaList): Boolean {
            return oldItem == newItem
        }
    }
}