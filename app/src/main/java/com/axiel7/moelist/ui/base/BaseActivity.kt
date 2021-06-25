package com.axiel7.moelist.ui.base

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.preference.PreferenceManager
import androidx.viewbinding.ViewBinding
import com.axiel7.moelist.R

abstract class BaseActivity<VB: ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!
    abstract val bindingInflater: (LayoutInflater) -> VB

    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        preCreate()
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(false)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    /**
     * Called before super.onCreate
     */
    protected open fun preCreate() {}

    protected open fun setDecorFitsSystemWindows(value: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, value)
    }

    /**
     * Called after setContentView in onCreate
     */
    protected abstract fun setup()

    private fun changeTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (sharedPreferences.getString("theme", "follow_system")=="amoled") {
            setTheme(R.style.AppTheme_Amoled)
        } else { setTheme(R.style.AppTheme) }
    }

    @ColorInt
    protected fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}