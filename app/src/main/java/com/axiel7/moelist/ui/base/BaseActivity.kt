package com.axiel7.moelist.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.axiel7.moelist.utils.Extensions.changeTheme
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.google.android.material.color.DynamicColors
import com.google.android.material.elevation.SurfaceColors
import kotlinx.coroutines.launch

abstract class BaseActivity<VB: ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!
    protected abstract val bindingInflater: (LayoutInflater) -> VB
    protected val sharedPref = SharedPrefsHelpers.instance!!

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this)
        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
        changeTheme()
        preCreate()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    /**
     * Called before super.onCreate
     */
    protected open fun preCreate() {}

    /**
     * Called after setContentView in onCreate
     */
    protected abstract fun setup()

    protected fun launchLifecycleStarted(launch: suspend () -> Unit) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch.invoke()
            }
        }
    }

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}