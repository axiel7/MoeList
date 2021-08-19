package com.axiel7.moelist.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.axiel7.moelist.R

class MaterialSpinnerAdapter<T>(
    context: Context,
    private val items: Array<T>
) : ArrayAdapter<T>(context, R.layout.item_spinner, items) {
    private val filter = NoFilter()

    override fun getFilter(): Filter {
        return filter
    }

    inner class NoFilter : Filter() {

        override fun performFiltering(p0: CharSequence?): FilterResults {
            return FilterResults().apply {
                values = items
                count = items.size
            }
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}