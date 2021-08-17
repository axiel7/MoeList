package com.axiel7.moelist.adapter.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BasePagingAdapter<VB: ViewBinding, T: Any>(diffCallback: DiffUtil.ItemCallback<T>)
    : PagingDataAdapter<T, BasePagingAdapter<VB, T>.ViewHolder>(diffCallback) {

    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = bindingInflater.invoke(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            loadData(holder, position, item)
        }
    }

    abstract fun loadData(holder: ViewHolder, position: Int, item: T)

    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}