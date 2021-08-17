package com.axiel7.moelist.utils

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView

object Extensions {

    /**
     * @return if true 1 else 0
     */
    fun Boolean?.toInt(): Int = if (this == true) 1 else 0

    /**
     * Returns a string representation of the object.
     * Can be called with a null receiver, in which case it returns null.
     */
    fun Any?.toStringOrNull() : String? {
        val result = this.toString()
        return if (result == "null") null else result
    }

    /**
     * Shows the soft input keyboard on a SearchView
     */
    fun SearchView.showKeyboard(context: Context) {
        val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        requestFocus()
        inputMethodManager.showSoftInput(this, 0)
    }
}