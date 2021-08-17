package com.axiel7.moelist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.databinding.ListHeaderBinding

class HeaderAdapter(
    private val onClickSort: (View) -> Unit
) : RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderAdapter.HeaderViewHolder {
        val binding = ListHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderAdapter.HeaderViewHolder, position: Int) {
        holder.binding.sortChip.setOnClickListener { onClickSort(it) }
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class HeaderViewHolder(val binding: ListHeaderBinding) : RecyclerView.ViewHolder(binding.root)

}