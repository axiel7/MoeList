package com.axiel7.moelist.adapter.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<VB: ViewBinding, T> : RecyclerView.Adapter<BaseAdapter<VB, T>.ViewHolder>() {

    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    private var _list: List<T>? = listOf()

    fun setData(list: List<T>) {
        _list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = bindingInflater.invoke(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = _list?.get(position)
        if (item != null) {
            loadData(holder, position, item)
        }
    }

    /**
     * Called onBindViewHolder
     */
    protected abstract fun loadData(holder: ViewHolder, position: Int, item: T)

    override fun getItemCount(): Int {
        return _list?.size ?: 0
    }

    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}