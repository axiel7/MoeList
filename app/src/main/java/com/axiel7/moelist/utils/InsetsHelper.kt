@file:Suppress("unused", "unused", "unused", "unused", "unused", "unused", "unused")

package com.axiel7.moelist.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.*
import com.google.android.material.bottomsheet.BottomSheetBehavior

object InsetsHelper {
    fun View.addSystemWindowInsetToPadding(
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        val (initialLeft, initialTop, initialRight, initialBottom) =
            listOf(paddingLeft, paddingTop, paddingRight, paddingBottom)

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            view.updatePadding(
                left = initialLeft + (if (left) insets.getInsets(WindowInsetsCompat.Type.systemBars()).left else 0),
                top = initialTop + (if (top) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0),
                right = initialRight + (if (right) insets.getInsets(WindowInsetsCompat.Type.systemBars()).right else 0),
                bottom = initialBottom + (if (bottom) insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom else 0)
            )

            insets
        }
    }

    fun View.addSystemWindowInsetToMargin(
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        val (initialLeft, initialTop, initialRight, initialBottom) =
            listOf(marginLeft, marginTop, marginRight, marginBottom)

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            view.updateLayoutParams {
                (this as? ViewGroup.MarginLayoutParams)?.let {
                    updateMargins(
                        left = initialLeft + (if (left) insets.getInsets(WindowInsetsCompat.Type.systemBars()).left else 0),
                        top = initialTop + (if (top) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0),
                        right = initialRight + (if (right) insets.getInsets(WindowInsetsCompat.Type.systemBars()).right else 0),
                        bottom = initialBottom + (if (bottom) insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom else 0)
                    )
                }
            }

            insets
        }
    }

    fun <T : ViewGroup> getViewBottomHeight(layout: ViewGroup,
                                            targetViewId: Int,
                                            behavior: BottomSheetBehavior<T>) {
        layout.apply {
            viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        behavior.peekHeight = findViewById<View>(targetViewId).bottom
                    }
                })
        }
    }

    fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
        layoutParams<ViewGroup.MarginLayoutParams> {
            left?.run { leftMargin = dpToPx(this) }
            top?.run { topMargin = dpToPx(this) }
            right?.run { rightMargin = dpToPx(this) }
            bottom?.run { bottomMargin = dpToPx(this) }
        }
    }

    inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
        if (layoutParams is T) block(layoutParams as T)
    }

    private fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
    private fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

}