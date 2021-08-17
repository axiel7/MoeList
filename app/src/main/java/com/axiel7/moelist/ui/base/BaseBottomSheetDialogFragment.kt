package com.axiel7.moelist.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

abstract class BaseBottomSheetDialogFragment<VB: ViewBinding> : BottomSheetDialogFragment() {

    protected lateinit var safeContext: Context
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    protected val sharedPref = SharedPrefsHelpers.instance!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
        onAttaching()
    }

    /**
     * Called after super.onAttach
     */
    open fun onAttaching() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    /**
     * Called after onViewCreated
     */
    abstract fun setup()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun launchLifecycleStarted(launch: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch.invoke()
            }
        }
    }

    protected fun showToast(message: String) {
        Toast.makeText(safeContext, message, Toast.LENGTH_SHORT).show()
    }

}