package com.axiel7.moelist.ui.base

import android.os.Build
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
import kotlinx.coroutines.launch
import java.util.*

abstract class BaseActivity<VB: ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!
    protected abstract val bindingInflater: (LayoutInflater) -> VB
    protected val sharedPref = SharedPrefsHelpers.instance!!

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        sharedPref.getString("app_language", null)?.let {
            if (it != "null") changeLocale(it)
        }
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

    private fun changeLocale(languageCode: String) {
        val config = resources.configuration
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}